package com.mustafakoceerr.justrelax.feature.scene

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SavedSceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.scene.generated.resources.Res
import justrelax.feature.scene.generated.resources.err_mix_save_empty_name
import justrelax.feature.scene.generated.resources.err_mix_save_name_exists
import justrelax.feature.scene.generated.resources.err_mix_save_no_sound
import justrelax.feature.scene.generated.resources.err_unknown
import justrelax.feature.scene.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The mini player shown above the bottom bar while sounds are active. */
class SceneViewModel(
    soundRepository: SoundRepository,
    private val sceneRepository: SceneRepository,
    private val playbackController: PlaybackController,
    private val savedSceneRepository: SavedSceneRepository,
) : ViewModel() {

    private data class ScreenState(
        val isSaveDialogVisible: Boolean = false,
        val isSaving: Boolean = false,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<SceneUiState> = combine(
        soundRepository.getSounds(),
        sceneRepository.scene,
        playbackController.isPlaying,
        screenState,
    ) { sounds, scene, isPlaying, screen ->
        val soundsById = sounds.associateBy { it.id }
        SceneUiState(
            activeSounds = scene.layers.mapNotNull { soundsById[it.soundId] },
            isPlaying = isPlaying,
            isSaveDialogVisible = screen.isSaveDialogVisible,
            isSaving = screen.isSaving,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SceneUiState())

    fun togglePlayPause() {
        if (playbackController.isPlaying.value) playbackController.pause() else playbackController.play()
    }

    fun stopAll() {
        sceneRepository.clear()
    }

    fun openSaveDialog() = screenState.update { it.copy(isSaveDialogVisible = true) }

    fun dismissSaveDialog() = screenState.update { it.copy(isSaveDialogVisible = false) }

    fun saveMix(name: String) {
        viewModelScope.launch {
            screenState.update { it.copy(isSaving = true) }
            val result = savedSceneRepository.save(name, sceneRepository.scene.value)
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
