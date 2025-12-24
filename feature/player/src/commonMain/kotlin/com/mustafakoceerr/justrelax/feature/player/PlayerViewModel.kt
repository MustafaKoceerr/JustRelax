package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.TogglePauseResumeUseCase
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerScreenModel(
    private val soundRepository: SoundRepository,
    private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
    private val stopAllSoundsUseCase: StopAllSoundsUseCase,
    private val togglePauseResumeUseCase: TogglePauseResumeUseCase
) : ScreenModel {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        observeActiveSounds()
    }

    /**
     * İki farklı Flow'u (Akışı) birleştiriyoruz:
     * 1. Veritabanındaki Tüm Sesler (soundRepository.getSounds)
     * 2. Çalan Seslerin ID'leri (getPlayingSoundsUseCase)
     *
     * Herhangi biri değiştiğinde blok çalışır ve UI state güncellenir.
     */
    private fun observeActiveSounds() {
        screenModelScope.launch {
            combine(
                soundRepository.getSounds(),
                getPlayingSoundsUseCase()
            ) { allSounds, playingSoundIds ->
                // ID listesini kullanarak, Sound nesnelerini bulup listeye çeviriyoruz.
                val activeSoundList = allSounds.filter { sound ->
                    playingSoundIds.contains(sound.id)
                }
                activeSoundList
            }.collect { activeList ->
                _state.update { currentState ->
                    currentState.copy(
                        activeSounds = activeList,
                        // Liste boşaldıysa (her şey durduysa) pause durumunu da sıfırla
                        isPaused = if (activeList.isEmpty()) false else currentState.isPaused
                    )
                }
            }
        }
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.StopAll -> {
                // Hepsini durdur (AudioMixer -> Service Stop)
                stopAllSoundsUseCase()
            }

            PlayerIntent.ToggleMasterPlayPause -> {
                toggleMasterPlayPause()
            }
        }
    }

    private fun toggleMasterPlayPause() {
        // Mevcut durumu tersine çevir (Optimistic Update)
        val newPausedState = !_state.value.isPaused

        _state.update { it.copy(isPaused = newPausedState) }

        if (newPausedState) {
            // Pause All
            togglePauseResumeUseCase.pauseAll()
        } else {
            // Resume All
            togglePauseResumeUseCase.resumeAll()
        }
    }
}