package com.mustafakoceerr.justrelax.feature.ai

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.GenerateAiMixUseCase
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiContract
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.err_ai_empty_response
import justrelax.feature.ai.generated.resources.err_ai_no_sounds
import justrelax.feature.ai.generated.resources.err_unknown
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val generateAiMixUseCase: GenerateAiMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    soundControllerFactory: SoundController.Factory
) : ScreenModel {

    // SoundController'ı bu ViewModel'in yaşam döngüsüne (scope) bağlıyoruz.
    val soundController: SoundController = soundControllerFactory.create(screenModelScope)

    private val _state = MutableStateFlow(AiContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<AiContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AiContract.Event) {
        when (event) {
            is AiContract.Event.UpdatePrompt -> {
                _state.update { it.copy(prompt = event.text) }
            }

            is AiContract.Event.SelectSuggestion -> {
                _state.update { it.copy(prompt = event.text) }
            }

            is AiContract.Event.GenerateMix -> generateMix()

            is AiContract.Event.RegenerateMix -> generateMix() // Aynı prompt ile tekrar dene

            is AiContract.Event.EditPrompt -> {
                // Back tuşu: Sonuçları temizle ama PROMPT KALSIN.
                _state.update {
                    it.copy(
                        generatedMixName = "",
                        generatedMixDescription = "",
                        generatedSounds = emptyList()
                    )
                }
            }

            is AiContract.Event.ClearMix -> {
                // Yeni Mix / Temizle: Her şeyi sıfırla (Prompt dahil).
                _state.update { AiContract.State() }
            }

            // --- SoundController Delegasyonu ---
            is AiContract.Event.ToggleSound -> {
                screenModelScope.launch {
                    soundController.toggleSound(event.soundId)
                }
            }

            is AiContract.Event.ChangeVolume -> {
                soundController.changeVolume(event.soundId, event.volume)
            }
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

                        // 1. Controller'a ses seviyelerini öğret (Cache)
                        val volumeMap = mix.sounds.map { (sound, volume) ->
                            sound.id to volume
                        }.toMap()
                        soundController.setVolumes(volumeMap)

                        // 2. Ses Motoruna "Çal" emrini ver
                        // (Arka planda paralel hazırlık yapılır)
                        setMixUseCase(mix.sounds)

                        // 3. UI State güncelle
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

    private fun handleError(error: AppError) {
        val message = when (error) {
            is AppError.Ai.NoDownloadedSounds -> UiText.Resource(Res.string.err_ai_no_sounds)
            is AppError.Ai.EmptyResponse -> UiText.Resource(Res.string.err_ai_empty_response)
            else -> UiText.Resource(Res.string.err_unknown)
        }
        sendEffect(AiContract.Effect.ShowSnackbar(message))
    }

    private fun sendEffect(effect: AiContract.Effect) {
        screenModelScope.launch { _effect.send(effect) }
    }
}