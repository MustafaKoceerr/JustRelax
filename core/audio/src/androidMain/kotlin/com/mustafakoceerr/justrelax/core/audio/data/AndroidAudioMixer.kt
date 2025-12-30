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
        // 1. Hızlı Kontrol (Kilitli): Zaten var mı?
        mutex.withLock {
            if (players.containsKey(config.id)) return Resource.Success(Unit)
            if (players.size >= AudioDefaults.MAX_CONCURRENT_SOUNDS) {
                val error = AppError.Player.LimitExceeded(AudioDefaults.MAX_CONCURRENT_SOUNDS)
                _state.update { it.copy(error = error.message) }
                return Resource.Error(error)
            }
        }

        // 2. Ağır İşlem (Kilitsiz): Player'ı oluştur.
        // Bu işlem sırasında UI donmaz ve diğer threadler kilitlenmez.
        val newWrapper = try {
            ExoPlayerWrapper(context)
        } catch (e: Exception) {
            return Resource.Error(AppError.Player.InitializationError(e.message ?: "Unknown"))
        }

        // 3. Ekleme ve Başlatma (Kilitli):
        return mutex.withLock {
            // Race Condition Kontrolü: Biz player'ı hazırlarken kullanıcı "StopAll" demiş olabilir.
            // Veya başka bir işlem araya girmiş olabilir.
            if (players.containsKey(config.id)) {
                newWrapper.stop() // Boşa oluşturduğumuzu temizle
                return@withLock Resource.Success(Unit)
            }

            // İlk ses ise servisi başlat
            if (players.isEmpty()) {
                serviceController.start()
            }

            players[config.id] = newWrapper

            // State güncelle
            _state.update {
                it.copy(isPlaying = true, activeSounds = it.activeSounds + config)
            }

            // Çalmaya başla (Wrapper içinde async çalışır)
            scope.launch { newWrapper.play(config) }

            Resource.Success(Unit)
        }
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

        if (players.isEmpty()) {
            serviceController.stop()
        }
    }

    override suspend fun stopAll() = mutex.withLock {
        if (players.isEmpty()) return@withLock

        players.values.forEach { it.stop() }
        players.clear()
        _state.value = GlobalMixerState()
        serviceController.stop()
    }

    override suspend fun setMix(configs: List<SoundConfig>) {
        // 1. Analiz (Kilitli): Hangileri kalacak, hangileri gidecek, hangileri yeni?
        val (toRemove, toAdd) = mutex.withLock {
            val currentIds = players.keys.toSet()
            val newIds = configs.map { it.id }.toSet()

            val removeList = currentIds - newIds
            val addList = configs.filter { !currentIds.contains(it.id) }

            Pair(removeList, addList)
        }

        // 2. Temizlik (Kilitli): Gereksizleri durdur ve sil
        if (toRemove.isNotEmpty()) {
            mutex.withLock {
                toRemove.forEach { id ->
                    players.remove(id)?.stop()
                }
            }
        }

        // 3. Paralel Hazırlık (Kilitsiz & Async):
        // 7 ses varsa 7'si aynı anda hazırlanır.
        if (toAdd.isNotEmpty()) {
            val newPlayersMap = withContext(Dispatchers.Main) {
                toAdd.map { config ->
                    async {
                        val player = ExoPlayerWrapper(context)
                        config to player
                    }
                }.awaitAll().toMap() // Hepsinin bitmesini bekle
            }

            // 4. Toplu Ekleme (Kilitli):
            mutex.withLock {
                // Servis kontrolü
                if (players.isEmpty() && newPlayersMap.isNotEmpty()) {
                    serviceController.start()
                }

                newPlayersMap.forEach { (config, player) ->
                    players[config.id] = player
                    // Hazır olanları çalmaya başlat
                    scope.launch { player.play(config) }
                }
            }
        }

        // 5. State Güncelleme (Son Durum)
        mutex.withLock {
            _state.update {
                it.copy(
                    isPlaying = configs.isNotEmpty(),
                    activeSounds = configs
                )
            }

            // Eğer liste boşaldıysa servisi durdur
            if (players.isEmpty()) {
                serviceController.stop()
            }
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




