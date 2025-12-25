package com.mustafakoceerr.justrelax.feature.ai

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.GenerateAiMixUseCase
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiState
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.err_ai_empty_response
import justrelax.feature.ai.generated.resources.err_ai_no_sounds
import justrelax.feature.ai.generated.resources.err_unknown
import justrelax.feature.ai.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiScreenModel(
    private val generateAiMixUseCase: GenerateAiMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    private val saveCurrentMixUseCase: SaveCurrentMixUseCase,
    soundControllerFactory: SoundController.Factory
) : ScreenModel {
    val soundController: SoundController = soundControllerFactory.create(screenModelScope)

    private val _state = MutableStateFlow(AiState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AiEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AiIntent) {
        when (intent) {
            is AiIntent.UpdatePrompt -> _state.update { it.copy(prompt = intent.text) }
            is AiIntent.SelectSuggestion -> _state.update { it.copy(prompt = intent.text) }

            is AiIntent.GenerateMix -> generateMix()
            is AiIntent.RegenerateMix -> generateMix() // EKLENDİ: Aynı fonksiyonu çağırır

            is AiIntent.EditPrompt -> {
                _state.update {
                    it.copy(
                        generatedMixName = "",
                        generatedMixDescription = "",
                        generatedSounds = emptyList()
                    )
                }
            }

            is AiIntent.ShowSaveDialog -> _state.update { it.copy(isSaveDialogVisible = true) }
            is AiIntent.HideSaveDialog -> _state.update { it.copy(isSaveDialogVisible = false) }
            is AiIntent.SaveMix -> saveMix(intent.name)
        }
    }

    private fun generateMix() {
        val prompt = _state.value.prompt
        if (prompt.isBlank()) return

        screenModelScope.launch {
            generateAiMixUseCase(prompt).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success -> {
                        val mix = result.data

                        // 1. Controller'ı güncelle (UI için)
                        val volumeMap =
                            mix.sounds.map { (sound, volume) -> sound.id to volume }.toMap()
                        soundController.setVolumes(volumeMap)

                        // 2. Player'ı güncelle (Ses için)
                        setMixUseCase(mix.sounds)

                        // 3. UI State güncelle (İsim ve Açıklama)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                generatedMixName = mix.name,
                                generatedMixDescription = mix.description,
                                generatedSounds = mix.sounds.keys.toList()
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        handleError(result.error)
                    }
                }
            }
        }
    }

    // Todo: Aynı işi mixer de yapıyor. codesmells, ikisini ortak bir controller'a koy.
    private fun saveMix(name: String) {
        screenModelScope.launch {
            saveCurrentMixUseCase(name, soundController).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(isSaveDialogVisible = false) }
                        sendEffect(
                            AiEffect.ShowSnackbar(
                                UiText.Resource(Res.string.msg_mix_saved_success, listOf(name))
                            )
                        )
                    }

                    is Resource.Error -> {
                        _state.update { it.copy(isSaveDialogVisible = false) }
                        handleError(result.error)
                    }

                    Resource.Loading -> {}
                }
            }
        }
    }

    private fun handleError(error: AppError) {
        val message = when (error) {
            is AppError.Ai.NoDownloadedSounds -> UiText.Resource(Res.string.err_ai_no_sounds)
            is AppError.Ai.EmptyResponse -> UiText.Resource(Res.string.err_ai_empty_response)
            else -> UiText.Resource(Res.string.err_unknown)
        }
        sendEffect(AiEffect.ShowSnackbar(message))
    }

    private fun sendEffect(effect: AiEffect) {
        screenModelScope.launch { _effect.send(effect) }
    }
}

