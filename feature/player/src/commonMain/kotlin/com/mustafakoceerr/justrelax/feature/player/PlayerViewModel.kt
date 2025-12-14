package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerScreenModel(
    private val soundManager: SoundManager
) : ScreenModel {

    // State
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        observeSoundManager()
    }

    private fun observeSoundManager() {
        // viewModelScope -> screenModelScope oldu
        screenModelScope.launch {
            soundManager.state.collectLatest { mixerState ->
                _state.update { currentState ->
                    currentState.copy(
                        activeSounds = mixerState.activeSounds.values.map { it.sound },
                        isMasterPlaying = mixerState.isMasterPlaying,
                        isLoading = mixerState.isLoading
                    )
                }
            }
        }
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.StopAll -> {
                soundManager.stopAll()
            }
            PlayerIntent.ToggleMasterPlayPause -> {
                screenModelScope.launch {
                    soundManager.toggleMasterPlayPause()
                }
            }
        }
    }
}