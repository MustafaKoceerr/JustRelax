package com.mustafakoceerr.justrelax.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepository
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.err_ai_empty_response
import justrelax.feature.ai.generated.resources.err_ai_no_sounds
import justrelax.feature.ai.generated.resources.err_unknown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiMixRepository: AiMixRepository,
    private val sceneRepository: SceneRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private data class ScreenState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val mix: AiGeneratedMix? = null,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<AiUiState> = combine(screenState, sceneRepository.scene, playbackController.isPlaying) { screen, scene, isPlaying ->
        AiUiState(
            prompt = screen.prompt,
            isLoading = screen.isLoading,
            mixName = screen.mix?.name.orEmpty(),
            mixDescription = screen.mix?.description.orEmpty(),
            mixSounds = screen.mix?.sounds?.keys?.toList().orEmpty(),
            // A paused mix shows its sounds as not playing.
            playingSoundIds = if (isPlaying) scene.layers.mapTo(mutableSetOf()) { it.soundId } else emptySet(),
            soundVolumes = scene.layers.associate { it.soundId to it.volume },
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiUiState())

    /** Typing in the field and picking a suggestion both set the prompt. */
    fun updatePrompt(text: String) = screenState.update { it.copy(prompt = text) }

    /** Generates a mix for the current prompt; also used to regenerate. */
    fun generateMix() {
        val prompt = screenState.value.prompt
        if (prompt.isBlank()) return

        viewModelScope.launch {
            aiMixRepository.generateMix(prompt).collect { result ->
                when (result) {
                    is Resource.Loading -> screenState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val mix = result.data
                        sceneRepository.replaceWith(mix.sounds.mapKeys { (sound, _) -> sound.id })
                        playbackController.play()
                        screenState.update { it.copy(isLoading = false, mix = mix) }
                    }
                    is Resource.Error -> screenState.update {
                        it.copy(isLoading = false, userMessage = result.error.toMessage())
                    }
                }
            }
        }
    }

    /** Back to the prompt, keeping what the user typed. */
    fun editPrompt() = screenState.update { it.copy(mix = null) }

    fun clearMix() = screenState.update { ScreenState() }

    fun toggleSound(soundId: String) {
        viewModelScope.launch {
            sceneRepository.toggle(soundId)
            if (sceneRepository.scene.value.layer(soundId) != null) playbackController.play()
        }
    }

    fun changeVolume(soundId: String, volume: Float) = sceneRepository.setVolume(soundId, volume)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private fun AppError.toMessage() = UiText.Resource(
        when (this) {
            is AppError.Ai.NoDownloadedSounds -> Res.string.err_ai_no_sounds
            is AppError.Ai.EmptyResponse -> Res.string.err_ai_empty_response
            else -> Res.string.err_unknown
        }
    )
}
