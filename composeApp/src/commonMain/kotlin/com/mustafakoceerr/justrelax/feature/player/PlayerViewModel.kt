package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel (
    private val soundPlayer: SoundPlayer
): ScreenModel{
    private val _state = MutableStateFlow(PlayerState())
    val state = _state.asStateFlow()

    fun processIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.ToggleSound -> toggleSound(intent.sound)
            is PlayerIntent.ChangeVolume -> changeVolume(intent.soundId, intent.volume)
            PlayerIntent.ToggleMasterPlayPause -> toggleMaster()
            PlayerIntent.StopAll -> stopAll()
        }
    }

    private fun toggleSound(sound: Sound){
        val currentActive = _state.value.activeSounds
        val currentDetails = _state.value.activeSoundDetails

        if (currentActive.containsKey(sound.id)){
            // Durdur
            soundPlayer.stop(sound.id)
            _state.update {
                it.copy(
                    activeSounds = currentActive - sound.id,
                    activeSoundDetails = currentDetails.filter { s -> s.id != sound.id },
                    isMasterPlaying = if ((currentActive - sound.id).isEmpty())  false else it.isMasterPlaying
                )
            }
        }else{
            // başlat.
            screenModelScope.launch {
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

    private fun changeVolume(soundId: String, volume:Float){
        soundPlayer.setVolume(soundId, volume)
        _state.update {
            val newMap = it.activeSounds.toMutableMap()
            newMap[soundId] = volume
            it.copy(activeSounds = newMap)
        }
    }

    private fun toggleMaster(){
        if (_state.value.isMasterPlaying){
            soundPlayer.pauseAll()
            _state.update { it.copy(isMasterPlaying = false) }
        }else{
            soundPlayer.resumeAll()
            _state.update { it.copy(isMasterPlaying = true) }
        }
    }

    private fun stopAll(){
        soundPlayer.stopAll()
        _state.update {
            it.copy(
                activeSounds = emptyMap(),
                activeSoundDetails = emptyList(),
                isMasterPlaying = false
            )
        }
    }

    override fun onDispose() {
        // Singleton olduğu için uygulama kapanana kadar çalışır.
        // Yine de temizlik iyidir.
        soundPlayer.release()
        super.onDispose()
    }
}