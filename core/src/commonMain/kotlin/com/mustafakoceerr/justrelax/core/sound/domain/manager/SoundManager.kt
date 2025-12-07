package com.mustafakoceerr.justrelax.core.sound.domain.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Bu manager player ve timer'ı aynı anda yönetmesi için oluşturulmuştur.
// Player view modelini timer'a inject ederek teknik borç almıştık. ancak şimdii
// bir üst katmana giderek timer ve sound'u soyutlayacağız.
// Bu sınıfın durumu (ViewModel'ler bunu dinleyecek)

data class SoundManagerState(
    val activeSounds: Map<String, Float> = emptyMap(),
    val activeSoundDetails: List<Sound> = emptyList(),
    val isMasterPlaying: Boolean = true
)

class SoundManager(
    private val soundPlayer: SoundPlayer
):SoundController {
    // Singleton olduğu için kendi scope'u olmalı.
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(SoundManagerState())
    val state = _state.asStateFlow()
    fun toggleSound(sound: Sound, initialVolume: Float = 0.5f) {
        val currentActive = _state.value.activeSounds
        val currentDetails = _state.value.activeSoundDetails

        if (currentActive.containsKey(sound.id)) {
            // Zaten çalıyorsa durdur
            soundPlayer.stop(sound.id)
            _state.update {
                it.copy(
                    activeSounds = currentActive - sound.id,
                    activeSoundDetails = currentDetails.filter { s -> s.id != sound.id },
                    isMasterPlaying = if ((currentActive - sound.id).isEmpty()) false else it.isMasterPlaying
                )
            }
        } else {
            // Çalmıyorsa, verilen ses seviyesi ile başlat
            scope.launch {
                // Hardcoded 0.5f yerine parametreden gelen değeri kullanıyoruz
                soundPlayer.play(sound, initialVolume)

                _state.update {
                    it.copy(
                        // State'e de bu özel ses seviyesini kaydediyoruz
                        activeSounds = currentActive + (sound.id to initialVolume),
                        activeSoundDetails = currentDetails + sound,
                        isMasterPlaying = true
                    )
                }
            }
        }
    }

    fun changeVolume(soundId: String, volume: Float) {
        soundPlayer.setVolume(soundId, volume)
        _state.update {
            val newMap = it.activeSounds.toMutableMap()
            newMap[soundId] = volume
            it.copy(activeSounds = newMap)
        }
    }

    fun toggleMaster() {
        if (_state.value.isMasterPlaying) {
            soundPlayer.pauseAll()
            _state.update { it.copy(isMasterPlaying = false) }
        } else {
            soundPlayer.resumeAll()
            _state.update { it.copy(isMasterPlaying = true) }
        }
    }

    override fun stopAll() {
        soundPlayer.stopAll()
        _state.update {
            it.copy(
                activeSounds = emptyMap(),
                activeSoundDetails = emptyList(),
                isMasterPlaying = false
            )
        }
    }

    fun release() {
        soundPlayer.release()
    }

    fun setMix(mixMap: Map<Sound, Float>){
        scope.launch {
            // 1. Player'a tek bir emir veriyoruz: "Bu listeyi çal"
            // Map'i List<Pair>'e çevirip gönderiyoruz
            soundPlayer.playMix(mixMap.toList())

            // 2. State'i TEK SEFERDE güncelliyoruz (UI 5 kere titremeyecek)
            _state.update {
                it.copy(
                    // State sadece ID ve Volume tutar.
                    activeSounds = mixMap.entries.associate { (sound, volume) -> sound.id to volume },
                    activeSoundDetails = mixMap.keys.toList(),
                    isMasterPlaying = true
                )
            }
        }
    }
}