package com.mustafakoceerr.justrelax.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepository
import com.mustafakoceerr.justrelax.core.data.repository.LegalRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.playback.toggleAndPlay
import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.ui.system.SystemLauncher
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_empty_response
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_no_sounds
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_unknown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiMixRepository: AiMixRepository,
    private val sceneRepository: SceneRepository,
    private val playbackController: PlaybackController,
    private val analytics: AnalyticsHelper,
    private val preferences: UserPreferencesRepository,
    private val legalRepository: LegalRepository,
    private val systemLauncher: SystemLauncher,
) : ViewModel() {

    private data class ScreenState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val mix: AiGeneratedMix? = null,
        val lastPrompt: String = "",
        val showAiDataNotice: Boolean = false,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<AiUiState> = combine(screenState, sceneRepository.scene, playbackController.isPlaying) { screen, scene, isPlaying ->
        AiUiState(
            prompt = screen.prompt,
            isLoading = screen.isLoading,
            mixName = screen.mix?.name.orEmpty(),
            mixDescription = screen.mix?.description.orEmpty(),
            lastPrompt = screen.lastPrompt,
            mixSounds = screen.mix?.sounds.orEmpty(),
            // A paused mix shows its sounds as not playing.
            sceneCards = SceneCards.of(scene, showPlaying = isPlaying),
            showAiDataNotice = screen.showAiDataNotice,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiUiState())

    /** Kept out of [uiState], so dragging a slider redraws only that slider and not the whole screen. */
    val volumes: StateFlow<Map<String, Float>> = sceneRepository.scene
        .map { it.volumes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun updatePrompt(text: String) = screenState.update { it.copy(prompt = text) }

    /** A suggestion is sent as soon as it is tapped. */
    fun sendSuggestion(text: String) {
        updatePrompt(text)
        generateMix()
    }

    /**
     * Generates a mix for the current prompt. The first time, the user is told that the prompt
     * goes to OpenAI, which may use it to improve its models, and nothing is sent until they agree.
     */
    fun generateMix() {
        if (screenState.value.prompt.isBlank()) return
        viewModelScope.launch {
            if (preferences.aiDataNoticeAccepted().first()) {
                requestMix()
            } else {
                screenState.update { it.copy(showAiDataNotice = true) }
            }
        }
    }

    /** Another mix for the prompt of the one on screen, even if the field was edited since. */
    fun regenerate() {
        val lastPrompt = screenState.value.lastPrompt
        if (lastPrompt.isBlank()) return
        updatePrompt(lastPrompt)
        generateMix()
    }

    fun acceptAiDataNotice() {
        screenState.update { it.copy(showAiDataNotice = false) }
        viewModelScope.launch {
            preferences.acceptAiDataNotice()
            requestMix()
        }
    }

    fun dismissAiDataNotice() = screenState.update { it.copy(showAiDataNotice = false) }

    fun openPrivacyPolicy() {
        viewModelScope.launch { systemLauncher.openUrl(legalRepository.getPrivacyPolicyUrl()) }
    }

    private suspend fun requestMix() {
        val prompt = screenState.value.prompt
        // Never the prompt itself: only that a mix was asked for, and why it failed.
        analytics.logEvent(AnalyticsEvent(EventTypes.AI_MIX_REQUESTED))

        aiMixRepository.generateMix(prompt).collect { result ->
            when (result) {
                is Resource.Loading -> screenState.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    val mix = result.data
                    sceneRepository.replaceWith(mix.scene)
                    playbackController.play()
                    screenState.update { it.copy(isLoading = false, mix = mix, lastPrompt = prompt) }
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

    fun clearMix() = screenState.update { ScreenState() }

    fun toggleSound(soundId: String) {
        viewModelScope.launch { sceneRepository.toggleAndPlay(soundId, playbackController) }
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
