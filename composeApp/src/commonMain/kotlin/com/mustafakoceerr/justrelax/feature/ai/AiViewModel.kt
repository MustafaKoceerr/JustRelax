package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.runtime.MutableState
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.error_generic
import com.mustafakoceerr.justrelax.composeapp.generated.resources.error_no_sounds_found
import com.mustafakoceerr.justrelax.feature.ai.data.AiService
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveDownloadedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.PlayAiMixUseCase
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiState
import com.mustafakoceerr.justrelax.utils.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiService: AiService,
    private val playAiMixUseCase: PlayAiMixUseCase, // Repository yerine UseCase
    private val observeDownloadedSoundsUseCase: ObserveDownloadedSoundsUseCase

) : ScreenModel {

    private val _state = MutableStateFlow(AiState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeDownloadedSounds()
    }

    private fun observeDownloadedSounds() {
        screenModelScope.launch {
            // UseCase'i çağırıyoruz
            observeDownloadedSoundsUseCase().collect { downloadedSounds ->
                val shouldShow = downloadedSounds.size < 20
                _state.update { it.copy(showDownloadSuggestion = shouldShow) }
            }
        }
    }

    fun processIntent(intent: AiIntent) {
        when (intent) {
            is AiIntent.GenerateMix -> generateMix(intent.prompt)
            is AiIntent.PlayMix -> playCurrentMix()
            is AiIntent.Reset -> _state.update { AiState() }
            is AiIntent.ClickDownloadSuggestion -> {
                screenModelScope.launch {
                    _effect.send(AiEffect.NavigateToHome)
                }
        }
    }
    }

    private fun generateMix(prompt: String){
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, generatedMix = null) }

            aiService.generateMix(prompt)
                .onSuccess { response ->
                    _state.update { it.copy(isLoading = false, generatedMix = response) }
                }
                .onFailure { error->
                    error.printStackTrace()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(Res.string.error_generic)
                        )
                    }
                }
        }
    }

    private fun playCurrentMix(){
        val mixData = _state.value.generatedMix ?: return

        screenModelScope.launch {
            // Logic tamamen UseCase'e devredildi.
            val isSuccess = playAiMixUseCase(mixData)

            if (!isSuccess){
                _state.update {
                    it.copy(error = UiText.StringResource(Res.string.error_no_sounds_found))
                }
            }
        }
    }

}