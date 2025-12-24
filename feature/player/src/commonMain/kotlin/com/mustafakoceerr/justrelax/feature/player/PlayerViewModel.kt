package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.ObservePlaybackStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PauseAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.ResumeAllSoundsUseCase
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
    private val observePlaybackStateUseCase: ObservePlaybackStateUseCase, // YENİ: Durumu buradan dinleyeceğiz
    private val stopAllSoundsUseCase: StopAllSoundsUseCase,
    private val pauseAllSoundsUseCase: PauseAllSoundsUseCase, // Toggle yerine net komutlar
    private val resumeAllSoundsUseCase: ResumeAllSoundsUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        screenModelScope.launch {
            // 3 Akışı Birleştiriyoruz (Single Source of Truth)
            combine(
                soundRepository.getSounds(),      // 1. Ses Verileri
                getPlayingSoundsUseCase(),        // 2. Hangi Sesler Aktif?
                observePlaybackStateUseCase()     // 3. Şu an Çalıyor mu? (Notification'dan değişebilir)
            ) { allSounds, playingSoundIds, isPlaying ->

                // Aktif ses nesnelerini bul
                val activeSoundList = allSounds.filter { sound ->
                    playingSoundIds.contains(sound.id)
                }

                // State'i oluştur
                PlayerState(
                    activeSounds = activeSoundList,
                    isPlaying = isPlaying // Direkt Mixer'dan gelen gerçek durum
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.StopAll -> {
                screenModelScope.launch { stopAllSoundsUseCase() }
            }

            PlayerIntent.ToggleMasterPlayPause -> {
                toggleMasterPlayPause()
            }
        }
    }

    private fun toggleMasterPlayPause() {
        // Şu anki GERÇEK duruma bakarak karar ver.
        // State zaten Mixer'dan beslendiği için günceldir.
        if (_state.value.isPlaying) {
            pauseAllSoundsUseCase()
        } else {
            resumeAllSoundsUseCase()
        }
    }
}