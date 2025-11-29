package com.mustafakoceerr.justrelax.core.sound.domain.manager

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
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
) {
    // Singleton olduğu için kendi scope'u olmalı.
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(SoundManagerState())
    val state = _state.asStateFlow()

    fun toggleSound(sound: Sound) {
        val currentActive = _state.value.activeSounds
        val currentDetails = _state.value.activeSoundDetails

        if (currentActive.containsKey(sound.id)) {
            soundPlayer.stop(sound.id)
            _state.update {
                it.copy(
                    activeSounds = currentActive - sound.id,
                    activeSoundDetails = currentDetails.filter { s -> s.id != sound.id },
                    isMasterPlaying = if ((currentActive - sound.id).isEmpty()) false else it.isMasterPlaying
                )
            }
        } else {
            scope.launch {
                soundPlayer.play(sound, 0.5f)
                _state.update {
                    it.copy(
                        activeSounds = currentActive + (sound.id to 0.5f),
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

    fun stopAll() {
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
}