package com.mustafakoceerr.justrelax.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.player.generated.resources.Res
import justrelax.feature.player.generated.resources.err_mix_save_empty_name
import justrelax.feature.player.generated.resources.err_mix_save_name_exists
import justrelax.feature.player.generated.resources.err_mix_save_no_sound
import justrelax.feature.player.generated.resources.err_unknown
import justrelax.feature.player.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The mini player shown above the bottom bar while sounds are active. */
class PlayerViewModel(
    soundRepository: SoundRepository,
    private val audioMixer: AudioMixer,
    private val saveCurrentMixUseCase: SaveCurrentMixUseCase,
) : ViewModel() {

    private data class ScreenState(
        val isSaveDialogVisible: Boolean = false,
        val isSaving: Boolean = false,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<PlayerUiState> = combine(
        soundRepository.getSounds(),
        audioMixer.state,
        screenState,
    ) { sounds, mixer, screen ->
        val soundsById = sounds.associateBy { it.id }
        PlayerUiState(
            activeSounds = mixer.activeSounds.mapNotNull { soundsById[it.id] },
            isPlaying = mixer.isPlaying,
            isSaveDialogVisible = screen.isSaveDialogVisible,
            isSaving = screen.isSaving,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState())

    fun togglePlayPause() {
        viewModelScope.launch {
            if (audioMixer.state.value.isPlaying) audioMixer.pauseAll() else audioMixer.resumeAll()
        }
    }

    fun stopAll() {
        viewModelScope.launch { audioMixer.stopAll() }
    }

    fun openSaveDialog() = screenState.update { it.copy(isSaveDialogVisible = true) }

    fun dismissSaveDialog() = screenState.update { it.copy(isSaveDialogVisible = false) }

    fun saveMix(name: String) {
        viewModelScope.launch {
            screenState.update { it.copy(isSaving = true) }
            val result = saveCurrentMixUseCase(name)
            screenState.update {
                it.copy(
                    isSaving = false,
                    isSaveDialogVisible = result !is Resource.Success,
                    userMessage = result.toMessage(name),
                )
            }
        }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private fun Resource<Unit>.toMessage(name: String): UiText? = when (this) {
        is Resource.Success -> UiText.Resource(Res.string.msg_mix_saved_success, listOf(name))
        is Resource.Error -> UiText.Resource(
            when (error) {
                is AppError.SaveMix.EmptyName -> Res.string.err_mix_save_empty_name
                is AppError.SaveMix.NameAlreadyExists -> Res.string.err_mix_save_name_exists
                is AppError.SaveMix.NoSoundsPlaying -> Res.string.err_mix_save_no_sound
                else -> Res.string.err_unknown
            }
        )
        Resource.Loading -> null
    }
}
