package com.mustafakoceerr.justrelax.core.audio.data

import android.content.Context
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * AudioMixer arayüzünün Android implementasyonu.
 * Uygulamanın ses motorunun beynidir.
 */
internal class AndroidAudioMixer(
    private val context: Context,
    private val serviceController: AudioServiceController
) : AudioMixer {

    private val _state = MutableStateFlow(GlobalMixerState())
    override val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val players = mutableMapOf<String, ExoPlayerWrapper>()
    private val mutex = Mutex()

    override suspend fun playSound(config: SoundConfig): Resource<Unit> = mutex.withLock {
        if (players.containsKey(config.id)) return@withLock Resource.Success(Unit)
        if (players.size >= AudioDefaults.MAX_CONCURRENT_SOUNDS) {
            val error = AppError.Player.LimitExceeded(AudioDefaults.MAX_CONCURRENT_SOUNDS)
            _state.update { it.copy(error = error.message) }
            return@withLock Resource.Error(error)
        }

        // EĞER İLK SES ÇALIYORSA, SERVİSİ BAŞLAT
        if (players.isEmpty()) {
            serviceController.start()
        }

        val wrapper = ExoPlayerWrapper(context)
        players[config.id] = wrapper

        _state.update { it.copy(isPlaying = true, activeSounds = it.activeSounds + config) }
        wrapper.play(config)
        return@withLock Resource.Success(Unit)
    }

    override suspend fun stopSound(soundId: String) = mutex.withLock {
        players.remove(soundId)?.stop()

        _state.update { currentState ->
            val newActiveSounds = currentState.activeSounds.filterNot { it.id == soundId }
            currentState.copy(
                activeSounds = newActiveSounds,
                isPlaying = newActiveSounds.isNotEmpty()
            )
        }

        // EĞER SON SES DE DURDUYSA, SERVİSİ DURDUR
        if (players.isEmpty()) {
            serviceController.stop()
        }
    }

    override suspend fun stopAll() = mutex.withLock {
        if (players.isEmpty()) return@withLock

        players.values.forEach { it.stop() }
        players.clear()
        _state.value = GlobalMixerState()

        // HER ŞEY DURDUĞU İÇİN SERVİSİ DURDUR
        serviceController.stop()
    }

    override suspend fun setMix(configs: List<SoundConfig>) = mutex.withLock {
        val newConfigMap = configs.associateBy { it.id }
        val currentIds = players.keys.toSet()

        (currentIds - newConfigMap.keys).forEach { idToStop ->
            players.remove(idToStop)?.stop()
        }

        newConfigMap.values.forEach { config ->
            if (!players.containsKey(config.id)) {
                val wrapper = ExoPlayerWrapper(context)
                players[config.id] = wrapper
                scope.launch { wrapper.play(config) }
            }
        }

        _state.update {
            it.copy(
                isPlaying = configs.isNotEmpty(),
                activeSounds = configs
            )
        }
    }

    override suspend fun pauseAll() = mutex.withLock {
        if (players.isEmpty() || !_state.value.isPlaying) return@withLock
        players.values.forEach { it.pause() }
        _state.update { it.copy(isPlaying = false) }
    }

    override suspend fun resumeAll() = mutex.withLock {
        if (players.isEmpty() || _state.value.isPlaying) return@withLock
        players.values.forEach { it.resume() }
        _state.update { it.copy(isPlaying = true) }
    }

    override fun setVolume(soundId: String, volume: Float) {
        players[soundId]?.setVolume(volume)
        // 2. State'i güncelle (UI'ı haberdar et)
        // Bu yapılmazsa UI eski değeri göstermeye devam eder ve slider çalışmaz.
        _state.update { currentState ->
            val updatedSounds = currentState.activeSounds.map { config ->
                if (config.id == soundId) {
                    // SoundConfig immutable olduğu için kopyasını alıp yeni volümü veriyoruz.
                    config.copy(initialVolume = volume)
                } else {
                    config
                }
            }
            currentState.copy(activeSounds = updatedSounds)
        }
    }

    override fun release() {
        scope.launch { stopAll() }
        scope.cancel()
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }
}




