package com.mustafakoceerr.justrelax.feature.ai

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.controller.SoundListController
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiService
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveActiveContextUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveDownloadedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.PlayAiMixUseCase
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiState
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiUiState
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.error_generic
import justrelax.feature.ai.generated.resources.error_mix_name_exists
import justrelax.feature.ai.generated.resources.error_no_sounds_found
import justrelax.feature.ai.generated.resources.error_no_sounds_to_save
import justrelax.feature.ai.generated.resources.success_mix_saved
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiService: AiService,
    private val playAiMixUseCase: PlayAiMixUseCase,
    private val observeDownloadedSoundsUseCase: ObserveDownloadedSoundsUseCase,
    private val observeActiveContextUseCase: ObserveActiveContextUseCase,
    private val savedMixRepository: SavedMixRepository,
    private val controllerFactory: SoundListController.Factory
) : ScreenModel {

    val soundListController = controllerFactory.create(screenModelScope)

    private val _state = MutableStateFlow(AiState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeDownloadedSounds()
        observeActiveSounds()
    }

    private fun observeDownloadedSounds() {
        screenModelScope.launch {
            observeDownloadedSoundsUseCase().collect { downloadedSounds ->
                _state.update { it.copy(showDownloadSuggestion = downloadedSounds.size < 20) }
            }
        }
    }

    private fun observeActiveSounds() {
        screenModelScope.launch {
            observeActiveContextUseCase().collect { activeSoundInfos ->
                _state.update {
                    it.copy(
                        activeSoundsInfo = activeSoundInfos,
                        isContextEnabled = if (activeSoundInfos.isEmpty()) false else it.isContextEnabled
                    )
                }
            }
        }
    }


    fun processIntent(intent: AiIntent) {
        when (intent) {
            is AiIntent.UpdatePrompt -> _state.update { it.copy(prompt = intent.text) }
            is AiIntent.SelectSuggestion -> _state.update { it.copy(prompt = intent.text) }
            is AiIntent.ToggleContext -> _state.update { it.copy(isContextEnabled = !it.isContextEnabled) }
            is AiIntent.EditPrompt -> _state.update { it.copy(uiState = AiUiState.IDLE) }
            is AiIntent.GenerateMix -> generateMix(isRegenerate = false)
            is AiIntent.RegenerateMix -> generateMix(isRegenerate = true)
            is AiIntent.ClickDownloadSuggestion -> screenModelScope.launch { _effect.send(AiEffect.NavigateToSettings) }
            is AiIntent.ConfirmSaveMix -> saveCurrentMix(intent.mixName)

            // DÜZELTME: Mixer gibi State üzerinden Dialog Yönetimi
            AiIntent.ShowSaveDialog -> {
                if (soundListController.activeSoundsState.value.isNotEmpty()) {
                    _state.update { it.copy(isSaveDialogVisible = true) }
                } else {
                    screenModelScope.launch {
                        _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.error_no_sounds_to_save)))
                    }
                }
            }
            // Dialog kapandığında (Contract'a bu intent'i eklemeyi unutma)
            is AiIntent.HideSaveDialog -> {
                _state.update { it.copy(isSaveDialogVisible = false) }
            }
        }
    }

    private fun generateMix(isRegenerate: Boolean) {
        val currentPrompt = state.value.prompt
        if (currentPrompt.isBlank() || state.value.uiState is AiUiState.LOADING) return

        screenModelScope.launch {
            _state.update { it.copy(uiState = AiUiState.LOADING) }

            // NOT: stopAll() kaldırıldı. Müzik kesilmeden devam eder.

            val finalPrompt = buildPromptWithContext(currentPrompt, isRegenerate)
            val result = aiService.generateMix(finalPrompt)

            result.onSuccess { mixResponse ->
                val generatedSounds = playAiMixUseCase(mixResponse)

                if (generatedSounds != null) {
                    _state.update {
                        it.copy(
                            uiState = AiUiState.SUCCESS(
                                mixName = mixResponse.mixName,
                                mixDescription = mixResponse.description,
                                sounds = generatedSounds
                            )
                        )
                    }
                } else {
                    val errorMsg = UiText.StringResource(Res.string.error_no_sounds_found)
                    _state.update { it.copy(uiState = AiUiState.ERROR(errorMsg)) }
                    _effect.send(AiEffect.ShowSnackbar(errorMsg))
                }
            }.onFailure { error ->
                val errorMsg = UiText.DynamicString(error.message ?: "Unknown Error")
                _state.update { it.copy(uiState = AiUiState.ERROR(errorMsg)) }
                _effect.send(AiEffect.ShowSnackbar(errorMsg))
            }
        }
    }
    private fun saveCurrentMix(mixName: String) {
        screenModelScope.launch {
            val activeSoundsMap = soundListController.activeSoundsState.value

            if (activeSoundsMap.isEmpty()) {
                _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.error_no_sounds_to_save)))
                return@launch
            }
            if (savedMixRepository.isMixNameExists(mixName)) {
                _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.error_mix_name_exists)))
                return@launch
            }

            savedMixRepository.saveMix(mixName, activeSoundsMap)

            // Başarılı olursa dialogu kapat
            _state.update { it.copy(isSaveDialogVisible = false) }
            _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.success_mix_saved, mixName)))
        }
    }

    private fun buildPromptWithContext(prompt: String, isRegenerate: Boolean): String {
        val activeSounds = state.value.activeSoundsInfo
        if (!state.value.isContextEnabled || isRegenerate || activeSounds.isEmpty()) {
            return prompt
        }
        val context = activeSounds.joinToString(", ") { "${it.name} (volume: ${it.volume})" }
        return "Currently playing sounds are: $context. Now, based on this context, handle the following request: \"$prompt\""
    }
}