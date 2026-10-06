package com.mustafakoceerr.justrelax.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.GenerateAiMixUseCase
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
    private val generateAiMixUseCase: GenerateAiMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    private val soundController: SoundController,
) : ViewModel() {

    private data class ScreenState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val mix: AiGeneratedMix? = null,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<AiUiState> = combine(screenState, soundController.state) { screen, mixer ->
        AiUiState(
            prompt = screen.prompt,
            isLoading = screen.isLoading,
            mixName = screen.mix?.name.orEmpty(),
            mixDescription = screen.mix?.description.orEmpty(),
            mixSounds = screen.mix?.sounds?.keys?.toList().orEmpty(),
            // A paused mix shows its sounds as not playing.
            playingSoundIds = if (mixer.isPlaying) mixer.activeSounds.mapTo(mutableSetOf()) { it.id } else emptySet(),
            soundVolumes = mixer.activeSounds.associate { it.id to it.initialVolume },
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
            generateAiMixUseCase(prompt).collect { result ->
                when (result) {
                    is Resource.Loading -> screenState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val mix = result.data
                        soundController.setVolumes(mix.sounds.mapKeys { (sound, _) -> sound.id })
                        setMixUseCase(mix.sounds)
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
        viewModelScope.launch { soundController.toggleSound(soundId) }
    }

    fun changeVolume(soundId: String, volume: Float) = soundController.changeVolume(soundId, volume)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private fun AppError.toMessage() = UiText.Resource(
        when (this) {
            is AppError.Ai.NoDownloadedSounds -> Res.string.err_ai_no_sounds
            is AppError.Ai.EmptyResponse -> Res.string.err_ai_empty_response
            else -> Res.string.err_unknown
        }
    )
}
