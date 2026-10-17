package com.mustafakoceerr.justrelax.core.audio.mixer

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
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

/**
 * Plays up to [AudioDefaults.MAX_CONCURRENT_SOUNDS] looping sounds at once.
 *
 * All structural changes happen under [mutex]. Preparing a sound is slow, so it runs outside the
 * lock; a slot is reserved in [pendingIds] first so concurrent calls cannot exceed the limit, and a
 * sound stopped while it was still preparing is released instead of being started.
 */
internal class DefaultAudioMixer(
    private val playerFactory: SoundPlayerFactory,
    private val serviceController: AudioServiceController,
    private val scope: CoroutineScope,
) : AudioMixer {

    private val _state = MutableStateFlow(GlobalMixerState())
    override val state = _state.asStateFlow()

    private val mutex = Mutex()

    // Immutable snapshot so the non-suspending setVolume can read it without taking the lock.
    private val players = MutableStateFlow<Map<String, SoundPlayer>>(emptyMap())
    private val startJobs = mutableMapOf<String, Job>()
    private val pendingIds = mutableSetOf<String>()

    override suspend fun playSound(config: SoundConfig): Resource<Unit> {
        mutex.withLock {
            if (config.id in players.value || config.id in pendingIds) return Resource.Success(Unit)
            if (players.value.size + pendingIds.size >= AudioDefaults.MAX_CONCURRENT_SOUNDS) {
                val error = AppError.Player.LimitExceeded(AudioDefaults.MAX_CONCURRENT_SOUNDS)
                _state.update { it.copy(error = error.message) }
                return Resource.Error(error)
            }
            pendingIds += config.id
        }

        val player = try {
            createPreparedPlayer(config)
        } catch (e: CancellationException) {
            mutex.withLock { pendingIds -= config.id }
            throw e
        } catch (e: Exception) {
            mutex.withLock { pendingIds -= config.id }
            return Resource.Error(AppError.Player.InitializationError(e.message ?: "Unknown"))
        }

        mutex.withLock {
            if (!pendingIds.remove(config.id)) {
                // Stopped while preparing.
                player.release()
                return Resource.Success(Unit)
            }
            if (!_state.value.isPlaying) resumePlayersLocked()
            addPlayerLocked(config, player)
            _state.update { it.copy(isPlaying = true, activeSounds = it.activeSounds + config) }
        }
        return Resource.Success(Unit)
    }

    override suspend fun stopSound(soundId: String) = mutex.withLock {
        pendingIds -= soundId
        removePlayerLocked(soundId)
        _state.update { current ->
            val remaining = current.activeSounds.filterNot { it.id == soundId }
            current.copy(activeSounds = remaining, isPlaying = remaining.isNotEmpty())
        }
    }

    override suspend fun stopAll() = mutex.withLock {
        pendingIds.clear()
        players.value.keys.forEach { removePlayerLocked(it) }
        _state.value = GlobalMixerState()
    }

    override suspend fun setMix(configs: List<SoundConfig>) {
        val mix = configs.distinctBy { it.id }.take(AudioDefaults.MAX_CONCURRENT_SOUNDS)
        val mixIds = mix.map { it.id }.toSet()

        val toAdd = mutex.withLock {
            pendingIds.clear()
            (players.value.keys - mixIds).forEach { removePlayerLocked(it) }
            mix.forEach { config -> players.value[config.id]?.setVolume(config.initialVolume) }
            mix.filter { it.id !in players.value }.also { added -> pendingIds += added.map { it.id } }
        }

        val prepared = coroutineScope {
            toAdd.map { config ->
                async {
                    val player = try {
                        createPreparedPlayer(config)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                    config to player
                }
            }.awaitAll()
        }

        mutex.withLock {
            if (!_state.value.isPlaying) resumePlayersLocked()
            prepared.forEach { (config, player) ->
                val stillWanted = pendingIds.remove(config.id)
                when {
                    player == null -> Unit
                    !stillWanted -> player.release()
                    else -> addPlayerLocked(config, player)
                }
            }
            val active = mix.filter { it.id in players.value }
            _state.update { it.copy(isPlaying = active.isNotEmpty(), activeSounds = active) }
        }
    }

    override suspend fun pauseAll() = mutex.withLock {
        if (players.value.isEmpty() || !_state.value.isPlaying) return@withLock
        // A fade-in that has not run yet would otherwise start the sound after the pause.
        startJobs.values.forEach { it.cancel() }
        startJobs.clear()
        players.value.values.forEach { it.pause() }
        _state.update { it.copy(isPlaying = false) }
    }

    override suspend fun resumeAll() = mutex.withLock {
        if (players.value.isEmpty() || _state.value.isPlaying) return@withLock
        resumePlayersLocked()
        _state.update { it.copy(isPlaying = true) }
    }

    override fun setVolume(soundId: String, volume: Float) {
        players.value[soundId]?.setVolume(volume)
        _state.update { current ->
            current.copy(activeSounds = current.activeSounds.map {
                if (it.id == soundId) it.copy(initialVolume = volume) else it
            })
        }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private suspend fun createPreparedPlayer(config: SoundConfig): SoundPlayer {
        val player = playerFactory.create()
        try {
            player.prepare(config)
        } catch (e: Throwable) {
            withContext(NonCancellable) { player.release() }
            throw e
        }
        return player
    }

    private fun addPlayerLocked(config: SoundConfig, player: SoundPlayer) {
        if (players.value.isEmpty()) serviceController.start()
        players.update { it + (config.id to player) }
        startJobs[config.id] = scope.launch { player.play(config.fadeInDurationMs) }
    }

    private suspend fun removePlayerLocked(soundId: String) {
        startJobs.remove(soundId)?.cancel()
        val player = players.value[soundId] ?: return
        players.update { it - soundId }
        player.release()
    }

    private suspend fun resumePlayersLocked() {
        players.value.values.forEach { it.resume() }
    }
}
