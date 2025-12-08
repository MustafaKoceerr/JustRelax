package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val soundManager: SoundManager, // ARTIK PLAYER DEĞİL MANAGER
    private val toggleSoundUseCase: ToggleSoundUseCase // UseCase geldi
) : ScreenModel {
    // İndirme durumunu kendi içimizde tutuyoruz (Local State)
    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())

    // Manager State ile Downloading State'i birleştiriyoruz (Combine)

    val state: StateFlow<PlayerState> = combine(
        soundManager.state,
        _downloadingIds
    ) { managerState, downloadingIds ->
        PlayerState(
            activeSounds = managerState.activeSounds,
            isMasterPlaying = managerState.isMasterPlaying,
            activeSoundDetails = managerState.activeSoundDetails,
            downloadingSoundIds = downloadingIds
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerState()
    )

    fun processIntent(intent: PlayerIntent) {
        // Gelen emri direkt Manager'a iletiyoruz
        when (intent) {
            is PlayerIntent.ToggleSound -> handleToggleSound(intent.sound)
            is PlayerIntent.ChangeVolume -> soundManager.changeVolume(intent.soundId, intent.volume)
            is PlayerIntent.ToggleMasterPlayPause -> soundManager.toggleMaster()
            is PlayerIntent.StopAll -> soundManager.stopAll()
        }
    }

    private fun handleToggleSound(sound: Sound) {
        screenModelScope.launch {
            toggleSoundUseCase(
                sound = sound,
                isCurrentlyDownloading = _downloadingIds.value.contains(sound.id)
            ).collect { result ->
                when (result) {
                    is ToggleSoundUseCase.Result.Downloading -> {
                        _downloadingIds.update { currentIds ->
                            if (result.isDownloading) currentIds + sound.id
                            else currentIds - sound.id
                        }
                    }
                    is ToggleSoundUseCase.Result.Error -> {
                        println("Hata: ${result.message}")
                    }
                    else -> Unit // Diğer durumlar (Toggled, Ignored) UI state'i etkilemez
                }
            }
        }
    }
}













