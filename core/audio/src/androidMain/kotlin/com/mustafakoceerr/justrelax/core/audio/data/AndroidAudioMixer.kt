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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * AudioMixer arayüzünün Android implementasyonu.
 * Uygulamanın ses motorunun beynidir.
 */
/**
 * AudioMixer arayüzünün Android implementasyonu.
 * PERFORMANS GÜNCELLEMESİ:
 * - Player oluşturma işlemleri Mutex dışına taşındı.
 * - setMix işlemi paralel (async/await) hale getirildi.
 */
internal class AndroidAudioMixer(
    private val context: Context,
    private val serviceController: AudioServiceController
) : AudioMixer {

    private val _state = MutableStateFlow(GlobalMixerState())
    override val state = _state.asStateFlow()

    // Main thread işlemleri için (ExoPlayer Main thread sever)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val players = mutableMapOf<String, ExoPlayerWrapper>()
    private val mutex = Mutex()

    override suspend fun playSound(config: SoundConfig): Resource<Unit> {
        // 1. Hızlı Kontrol
        mutex.withLock {
            if (players.containsKey(config.id)) return Resource.Success(Unit)
            if (players.size >= AudioDefaults.MAX_CONCURRENT_SOUNDS) {
                val error = AppError.Player.LimitExceeded(AudioDefaults.MAX_CONCURRENT_SOUNDS)
                _state.update { it.copy(error = error.message) }
                return Resource.Error(error)
            }
        }

        // 2. Arka Planda Hazırlık (IO Thread)
        // Wrapper oluşturulur ve Player inşa edilir. UI Thread serbesttir.
        val newWrapper = try {
            val wrapper = ExoPlayerWrapper(context)
            wrapper.prepare(config) // Bu fonksiyon artık suspend ve IO-safe
            wrapper
        } catch (e: Exception) {
            return Resource.Error(AppError.Player.InitializationError(e.message ?: "Unknown"))
        }

        // 3. Ekleme ve Başlatma (Main Thread + Mutex)
        return mutex.withLock {
            if (players.containsKey(config.id)) {
                newWrapper.stop()
                return@withLock Resource.Success(Unit)
            }

            if (players.isEmpty()) {
                serviceController.start()
            }

            players[config.id] = newWrapper

            _state.update {
                it.copy(isPlaying = true, activeSounds = it.activeSounds + config)
            }

            // Sadece "Play" komutu verilir (Hafif işlem)
            scope.launch { newWrapper.playFadeIn(config.fadeInDurationMs) }

            Resource.Success(Unit)
        }
    }

    override suspend fun stopSound(soundId: String) = mutex.withLock {
        players.remove(soundId)?.stop()
        _state.update { currentState ->
            val newActiveSounds = currentState.activeSounds.filterNot { it.id == soundId }
            currentState.copy(activeSounds = newActiveSounds, isPlaying = newActiveSounds.isNotEmpty())
        }
        if (players.isEmpty()) serviceController.stop()
    }

    override suspend fun stopAll() = mutex.withLock {
        if (players.isEmpty()) return@withLock

        players.values.forEach { it.stop() }
        players.clear()
        _state.value = GlobalMixerState()
        serviceController.stop()
    }

    override suspend fun setMix(configs: List<SoundConfig>) {
        // 1. Analiz
        val (toRemove, toAdd) = mutex.withLock {
            val currentIds = players.keys.toSet()
            val newIds = configs.map { it.id }.toSet()
            Pair(currentIds - newIds, configs.filter { !currentIds.contains(it.id) })
        }

        // 2. Temizlik
        if (toRemove.isNotEmpty()) {
            mutex.withLock {
                toRemove.forEach { players.remove(it)?.stop() }
            }
        }

        // 3. Paralel Hazırlık (IO Thread - ASYNC)
        // 7 ses varsa 7'si de IO thread'inde, Main Looper'a bağlı olarak oluşturulur.
        if (toAdd.isNotEmpty()) {
            val newPlayersMap = withContext(Dispatchers.IO) {
                toAdd.map { config ->
                    async {
                        val wrapper = ExoPlayerWrapper(context)
                        wrapper.prepare(config) // Ağır iş burada
                        config to wrapper
                    }
                }.awaitAll().toMap()
            }

            // 4. Toplu Başlatma (Main Thread)
            mutex.withLock {
                if (players.isEmpty() && newPlayersMap.isNotEmpty()) {
                    serviceController.start()
                }

                newPlayersMap.forEach { (config, wrapper) ->
                    players[config.id] = wrapper
                    scope.launch { wrapper.playFadeIn(config.fadeInDurationMs) }
                }
            }
        }

        // 5. State Güncelleme
        mutex.withLock {
            _state.update {
                it.copy(isPlaying = configs.isNotEmpty(), activeSounds = configs)
            }
            if (players.isEmpty()) serviceController.stop()
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

        _state.update { currentState ->
            val updatedSounds = currentState.activeSounds.map { config ->
                if (config.id == soundId) config.copy(initialVolume = volume) else config
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




