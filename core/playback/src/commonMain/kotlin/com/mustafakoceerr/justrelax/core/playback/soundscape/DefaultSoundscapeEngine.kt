package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.TimeSource

/**
 * Beds loop on [SoundPlayer]s; events are loaded into the [OneShotPlayer] and played at random
 * intervals by an [EventScheduler].
 *
 * All structural changes happen under [mutex]. Preparing a layer is slow, so it runs outside the
 * lock; its id is reserved in [pendingIds] first, and a layer that left the scene while it was
 * preparing is released instead of being attached.
 */
internal class DefaultSoundscapeEngine(
    private val bedPlayerFactory: SoundPlayerFactory,
    private val eventPlayer: OneShotPlayer,
    private val scope: CoroutineScope,
    private val random: Random = Random.Default,
    timeSource: TimeSource = TimeSource.Monotonic,
) : SoundscapeEngine {

    private class Bed(val player: SoundPlayer, var volume: Float, var started: Boolean = false)

    private val _activeSoundIds = MutableStateFlow<Set<String>>(emptySet())
    override val activeSoundIds: StateFlow<Set<String>> = _activeSoundIds.asStateFlow()

    private val mutex = Mutex()
    private val scheduler = EventScheduler(scope, eventPlayer, random, timeSource).apply { pause() }

    private var target = PlayableScene.EMPTY
    private val beds = mutableMapOf<String, Bed>()
    private val events = mutableMapOf<String, PlayableLayer.Event>()
    private val startJobs = mutableMapOf<String, Job>()
    private val pendingIds = mutableSetOf<String>()
    private var running = false
    private var masterVolume = 1f

    /** What [prepare] produced for a layer, ready to attach under the lock. */
    private sealed interface Prepared {
        class Bed(val player: SoundPlayer) : Prepared
        data object Event : Prepared
    }

    override suspend fun apply(scene: PlayableScene) {
        val toPrepare = mutex.withLock {
            target = scene
            (attachedIdsLocked() - scene.layers.map { it.soundId }.toSet()).forEach { detachLocked(it) }
            scene.beds.forEach { layer -> beds[layer.soundId]?.let { it.volume = layer.volume; applyVolume(it) } }
            scene.events.filter { it.soundId in events }.forEach { events[it.soundId] = it }
            syncEventsLocked()
            publishLocked()

            scene.layers.filter { it.soundId !in attachedIdsLocked() && it.soundId !in pendingIds }
                .also { layers -> pendingIds += layers.map { it.soundId } }
        }
        if (toPrepare.isEmpty()) return

        val prepared = coroutineScope {
            toPrepare.map { layer -> async { layer.soundId to prepareOrNull(layer) } }.awaitAll()
        }

        mutex.withLock {
            prepared.forEach { (soundId, result) ->
                pendingIds -= soundId
                val wanted = target.layer(soundId)
                when {
                    result == null -> Unit
                    wanted == null -> release(soundId, result)
                    else -> attachLocked(wanted, result)
                }
            }
            syncEventsLocked()
            publishLocked()
        }
    }

    override suspend fun setRunning(running: Boolean) = mutex.withLock {
        if (this.running == running) return@withLock
        this.running = running
        if (running) {
            beds.forEach { (soundId, bed) -> if (bed.started) bed.player.resume() else startLocked(soundId, bed) }
            scheduler.resume()
            eventPlayer.resumeAll()
        } else {
            // A fade-in that has not run yet would otherwise start the sound after the pause.
            startJobs.values.forEach { it.cancel() }
            startJobs.clear()
            beds.values.forEach { it.player.pause() }
            scheduler.pause()
            eventPlayer.pauseAll()
        }
    }

    override suspend fun setMasterVolume(volume: Float) = mutex.withLock {
        masterVolume = volume.coerceIn(0f, 1f)
        beds.values.forEach(::applyVolume)
        syncEventsLocked()
    }

    override suspend fun stop() = mutex.withLock {
        pendingIds.clear()
        target = PlayableScene.EMPTY
        scheduler.stop()
        eventPlayer.stopAll()
        attachedIdsLocked().forEach { detachLocked(it) }
        publishLocked()
    }

    private suspend fun prepareOrNull(layer: PlayableLayer): Prepared? = try {
        prepare(layer)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private suspend fun prepare(layer: PlayableLayer): Prepared = when (layer) {
        is PlayableLayer.Bed -> {
            val player = bedPlayerFactory.create()
            try {
                player.prepare(layer.clip, layer.volume * masterVolume)
            } catch (e: Throwable) {
                withContext(NonCancellable) { player.release() }
                throw e
            }
            Prepared.Bed(player)
        }
        is PlayableLayer.Event -> {
            eventPlayer.load(layer.soundId, layer.clips)
            Prepared.Event
        }
    }

    private suspend fun release(soundId: String, prepared: Prepared) = when (prepared) {
        is Prepared.Bed -> prepared.player.release()
        Prepared.Event -> eventPlayer.unload(soundId)
    }

    private fun attachLocked(layer: PlayableLayer, prepared: Prepared) {
        when {
            prepared is Prepared.Bed && layer is PlayableLayer.Bed -> {
                val bed = Bed(prepared.player, layer.volume)
                beds[layer.soundId] = bed
                applyVolume(bed)
                if (running) startLocked(layer.soundId, bed)
            }
            layer is PlayableLayer.Event -> events[layer.soundId] = layer
        }
    }

    private fun startLocked(soundId: String, bed: Bed) {
        bed.started = true
        startJobs[soundId] = scope.launch {
            bed.player.play(AudioDefaults.FADE_IN_MS, startAtFraction = random.nextFloat())
        }
    }

    private suspend fun detachLocked(soundId: String) {
        startJobs.remove(soundId)?.cancel()
        beds.remove(soundId)?.player?.release()
        if (events.remove(soundId) != null) eventPlayer.unload(soundId)
    }

    private fun applyVolume(bed: Bed) = bed.player.setVolume(bed.volume * masterVolume)

    private fun syncEventsLocked() =
        scheduler.sync(events.values.map { it.copy(volume = it.volume * masterVolume) })

    private fun attachedIdsLocked(): Set<String> = beds.keys + events.keys

    private fun publishLocked() {
        _activeSoundIds.value = attachedIdsLocked()
    }
}
