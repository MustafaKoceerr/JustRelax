package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepository
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_empty_response
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_no_sounds
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_unknown
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
    private val analytics: AnalyticsHelper,
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
            mixSounds = screen.mix?.sounds.orEmpty(),
            // A paused mix shows its sounds as not playing.
            playingSoundIds = if (isPlaying) scene.layers.mapTo(mutableSetOf()) { it.soundId } else emptySet(),
            soundVolumes = scene.layers.associate { it.soundId to it.volume },
            soundFrequencies = scene.events.associate { it.soundId to it.frequency },
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiUiState())

    /** Typing in the field and picking a suggestion both set the prompt. */
    fun updatePrompt(text: String) = screenState.update { it.copy(prompt = text) }

    /** Generates a mix for the current prompt; also used to regenerate. */
    fun generateMix() {
        val prompt = screenState.value.prompt
        if (prompt.isBlank()) return
        // Never the prompt itself: only that a mix was asked for, and why it failed.
        analytics.logEvent(AnalyticsEvent(EventTypes.AI_MIX_REQUESTED))

        viewModelScope.launch {
            aiMixRepository.generateMix(prompt).collect { result ->
                when (result) {
                    is Resource.Loading -> screenState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val mix = result.data
                        sceneRepository.replaceWith(mix.scene)
                        playbackController.play()
                        screenState.update { it.copy(isLoading = false, mix = mix) }
                    }
                    is Resource.Error -> {
                        analytics.logEvent(
                            AnalyticsEvent(EventTypes.AI_MIX_FAILED, mapOf(Params.ERROR to (result.error::class.simpleName ?: "Unknown"))),
                        )
                        screenState.update { it.copy(isLoading = false, userMessage = result.error.toMessage()) }
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

    fun changeFrequency(soundId: String, frequency: EventFrequency) = sceneRepository.setFrequency(soundId, frequency)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private fun AppError.toMessage() = UiText.Resource(
        when (this) {
            is AppError.Ai.NoDownloadedSounds -> Res.string.err_ai_no_sounds
            is AppError.Ai.EmptyResponse -> Res.string.err_ai_empty_response
            else -> Res.string.err_unknown
        }
    )
}
