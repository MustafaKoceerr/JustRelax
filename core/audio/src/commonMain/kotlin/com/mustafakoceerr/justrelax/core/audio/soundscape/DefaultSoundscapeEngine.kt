package com.mustafakoceerr.justrelax.core.audio.soundscape

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.domain.player.SoundscapeState
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.TimeSource

/**
 * Plays a scene of up to [Scene.MAX_LAYERS] layers: beds loop on [SoundPlayer]s, events are
 * loaded into the [OneShotPlayer] and played at random intervals by an [EventScheduler].
 *
 * All structural changes happen under [mutex]. Preparing a layer is slow, so it runs outside the
 * lock; a slot is reserved in [pendingIds] first so concurrent calls cannot exceed the limit, and a
 * layer removed while it was still preparing is released instead of being started.
 */
internal class DefaultSoundscapeEngine(
    private val bedPlayerFactory: SoundPlayerFactory,
    private val eventPlayer: OneShotPlayer,
    private val serviceController: AudioServiceController,
    private val scope: CoroutineScope,
    private val random: Random = Random.Default,
    timeSource: TimeSource = TimeSource.Monotonic,
) : SoundscapeEngine {

    private val _state = MutableStateFlow(SoundscapeState())
    override val state = _state.asStateFlow()

    private val mutex = Mutex()
    private val scheduler = EventScheduler(scope, eventPlayer, random, timeSource)

    // Immutable snapshot so the non-suspending setVolume can read it without taking the lock.
    private val bedPlayers = MutableStateFlow<Map<String, SoundPlayer>>(emptyMap())
    private val loadedEvents = mutableSetOf<String>()
    private val startJobs = mutableMapOf<String, Job>()
    private val pendingIds = mutableSetOf<String>()

    /** What [prepare] produced for a layer, ready to attach under the lock. */
    private sealed interface Prepared {
        class Bed(val player: SoundPlayer) : Prepared
        data object Event : Prepared
    }

    private val scene: Scene get() = _state.value.scene
    private val hasAttachedLayers: Boolean get() = bedPlayers.value.isNotEmpty() || loadedEvents.isNotEmpty()

    override suspend fun addLayer(layer: Layer): Resource<Unit> {
        mutex.withLock {
            if (scene.layer(layer.soundId) != null || layer.soundId in pendingIds) return Resource.Success(Unit)
            if (scene.layers.size + pendingIds.size >= Scene.MAX_LAYERS) {
                val error = AppError.Player.LimitExceeded(Scene.MAX_LAYERS)
                _state.update { it.copy(error = error.message) }
                return Resource.Error(error)
            }
            pendingIds += layer.soundId
        }

        val prepared = try {
            prepare(layer)
        } catch (e: CancellationException) {
            mutex.withLock { pendingIds -= layer.soundId }
            throw e
        } catch (e: Exception) {
            mutex.withLock { pendingIds -= layer.soundId }
            return Resource.Error(AppError.Player.InitializationError(e.message ?: "Unknown"))
        }

        mutex.withLock {
            if (!pendingIds.remove(layer.soundId)) {
                // Removed while preparing.
                release(layer.soundId, prepared)
                return Resource.Success(Unit)
            }
            if (!_state.value.isPlaying) resumeLocked()
            attachLocked(layer, prepared)
            _state.update { it.copy(isPlaying = true, scene = it.scene.add(layer)) }
            syncEventsLocked()
        }
        return Resource.Success(Unit)
    }

    override suspend fun removeLayer(soundId: String) = mutex.withLock {
        pendingIds -= soundId
        detachLocked(soundId)
        _state.update { current ->
            val remaining = current.scene.remove(soundId)
            current.copy(scene = remaining, isPlaying = remaining.layers.isNotEmpty() && current.isPlaying)
        }
        syncEventsLocked()
    }

    override suspend fun stopAll() = mutex.withLock {
        pendingIds.clear()
        scheduler.stop()
        eventPlayer.stopAll()
        (bedPlayers.value.keys + loadedEvents).toList().forEach { detachLocked(it) }
        _state.value = SoundscapeState()
    }

    override suspend fun play(scene: Scene) {
        val targetIds = scene.layers.map { it.soundId }.toSet()

        val toAdd = mutex.withLock {
            pendingIds.clear()
            (this.scene.layers.map { it.soundId } - targetIds).forEach { detachLocked(it) }
            scene.beds.forEach { bed -> bedPlayers.value[bed.soundId]?.setVolume(bed.volume) }
            scene.layers.filter { it.soundId !in attachedIdsLocked() }
                .also { added -> pendingIds += added.map { it.soundId } }
        }

        val prepared = coroutineScope {
            toAdd.map { layer ->
                async {
                    val result = try {
                        prepare(layer)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                    layer to result
                }
            }.awaitAll()
        }

        mutex.withLock {
            if (!_state.value.isPlaying) resumeLocked()
            prepared.forEach { (layer, result) ->
                val stillWanted = pendingIds.remove(layer.soundId)
                when {
                    result == null -> Unit
                    !stillWanted -> release(layer.soundId, result)
                    else -> attachLocked(layer, result)
                }
            }
            val attached = attachedIdsLocked()
            val active = Scene(scene.layers.filter { it.soundId in attached })
            _state.update { it.copy(isPlaying = active.layers.isNotEmpty(), scene = active) }
            syncEventsLocked()
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        bedPlayers.value[soundId]?.setVolume(volume)
        _state.update { it.copy(scene = it.scene.withVolume(soundId, volume)) }
        // Event volumes apply from their next occurrence.
        scope.launch { mutex.withLock { syncEventsLocked() } }
    }

    override suspend fun setFrequency(soundId: String, frequency: EventFrequency) = mutex.withLock {
        _state.update { it.copy(scene = it.scene.withFrequency(soundId, frequency)) }
        syncEventsLocked()
    }

    override suspend fun pauseAll() = mutex.withLock {
        if (!hasAttachedLayers || !_state.value.isPlaying) return@withLock
        // A fade-in that has not run yet would otherwise start the sound after the pause.
        startJobs.values.forEach { it.cancel() }
        startJobs.clear()
        bedPlayers.value.values.forEach { it.pause() }
        scheduler.pause()
        eventPlayer.pauseAll()
        _state.update { it.copy(isPlaying = false) }
    }

    override suspend fun resumeAll() = mutex.withLock {
        if (!hasAttachedLayers || _state.value.isPlaying) return@withLock
        resumeLocked()
        _state.update { it.copy(isPlaying = true) }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private suspend fun prepare(layer: Layer): Prepared = when (layer) {
        is Layer.Bed -> {
            val player = bedPlayerFactory.create()
            try {
                player.prepare(layer.clip, layer.volume)
            } catch (e: Throwable) {
                withContext(NonCancellable) { player.release() }
                throw e
            }
            Prepared.Bed(player)
        }
        is Layer.Event -> {
            eventPlayer.load(layer.soundId, layer.clips)
            Prepared.Event
        }
    }

    private suspend fun release(soundId: String, prepared: Prepared) = when (prepared) {
        is Prepared.Bed -> prepared.player.release()
        Prepared.Event -> eventPlayer.unload(soundId)
    }

    private fun attachLocked(layer: Layer, prepared: Prepared) {
        if (!hasAttachedLayers) serviceController.start()
        when (prepared) {
            is Prepared.Bed -> {
                bedPlayers.update { it + (layer.soundId to prepared.player) }
                startJobs[layer.soundId] = scope.launch {
                    prepared.player.play(AudioDefaults.FADE_IN_MS, startAtFraction = random.nextFloat())
                }
            }
            Prepared.Event -> loadedEvents += layer.soundId
        }
    }

    private suspend fun detachLocked(soundId: String) {
        startJobs.remove(soundId)?.cancel()
        bedPlayers.value[soundId]?.let { player ->
            bedPlayers.update { it - soundId }
            player.release()
        }
        if (loadedEvents.remove(soundId)) eventPlayer.unload(soundId)
    }

    private suspend fun resumeLocked() {
        bedPlayers.value.values.forEach { it.resume() }
        scheduler.resume()
        eventPlayer.resumeAll()
    }

    private fun syncEventsLocked() = scheduler.sync(_state.value.scene.events)

    private fun attachedIdsLocked(): Set<String> = bedPlayers.value.keys + loadedEvents
}
