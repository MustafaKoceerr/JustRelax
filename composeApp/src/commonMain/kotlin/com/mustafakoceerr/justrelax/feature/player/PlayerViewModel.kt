package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel (
    private val soundManager: SoundManager // ARTIK PLAYER DEĞİL MANAGER
): ScreenModel{


    // Manager'ın state'ini dinleyip UI State'ine çeviriyoruz.
    // Böylece Manager'da bir şey değişince UI anında güncellenir.
    val state: StateFlow<PlayerState> = soundManager.state
        .map { managerState ->
            PlayerState(
                activeSounds = managerState.activeSounds,
                isMasterPlaying = managerState.isMasterPlaying,
                activeSoundDetails = managerState.activeSoundDetails
            )
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerState()
        )

    fun processIntent(intent: PlayerIntent) {
        // Gelen emri direkt Manager'a iletiyoruz
        when (intent) {
            is PlayerIntent.ToggleSound -> soundManager.toggleSound(intent.sound)
            is PlayerIntent.ChangeVolume -> soundManager.changeVolume(intent.soundId, intent.volume)
            is PlayerIntent.ToggleMasterPlayPause -> soundManager.toggleMaster()
            is PlayerIntent.StopAll -> soundManager.stopAll()
        }
    }
}