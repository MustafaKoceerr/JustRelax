package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.controller.SoundListController
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.GetDownloadedSoundCountUseCase
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerState
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.usecase.SaveMixResult
import com.mustafakoceerr.justrelax.feature.mixer.usecase.SaveMixUseCase
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.error_mix_name_empty
import justrelax.feature.mixer.generated.resources.error_mix_name_exists
import justrelax.feature.mixer.generated.resources.error_no_sounds
import justrelax.feature.mixer.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val saveMixUseCase: SaveMixUseCase,
    private val getDownloadedSoundCountUseCase: GetDownloadedSoundCountUseCase,
    private val controllerFactory: SoundListController.Factory // <--- Factory
) : ScreenModel {

    // 1. Controller
    val soundListController = controllerFactory.create(screenModelScope)

    private val _mixerState = MutableStateFlow(MixerState())

    // DÜZELTME: combine kaldırıldı. Artık sadece kendi state'ini sunuyor.
    val state = _mixerState.asStateFlow()

    private val _effect = Channel<MixerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        screenModelScope.launch {
            getDownloadedSoundCountUseCase().collectLatest { count ->
                // Todo: bu count'u bir use case'e koyabiliriz ya da %80 gibi belirleyebiliriz.
                _mixerState.update { it.copy(showDownloadSuggestion = count < 20) }
            }
        }
    }

    fun processIntent(intent: MixerIntent) {
        when (intent) {
            is MixerIntent.SelectCount -> _mixerState.update { it.copy(selectedCount = intent.count) }
            is MixerIntent.CreateMix -> createMix()
            is MixerIntent.HideSaveDialog -> _mixerState.update { it.copy(isSaveDialogVisible = false) }
            is MixerIntent.ClickDownloadSuggestion -> screenModelScope.launch {
                _effect.send(
                    MixerEffect.NavigateToSettings
                )
            }

            is MixerIntent.ConfirmSaveMix -> saveMixToDb(intent.name)

            is MixerIntent.ShowSaveDialog -> {
                // Controller state'inden kontrol ediyoruz
                if (soundListController.activeSoundsState.value.isNotEmpty()) {
                    _mixerState.update { it.copy(isSaveDialogVisible = true) }
                } else {
                    screenModelScope.launch {
                        _effect.send(MixerEffect.ShowSnackbar(UiText.StringResource(Res.string.error_no_sounds)))
                    }
                }
            }
        }
    }

    private fun createMix() {
        if (_mixerState.value.isLoading) return
        screenModelScope.launch {
            _mixerState.update { it.copy(isLoading = true) }
            val mixMap = generateRandomMixUseCase(_mixerState.value.selectedCount)

            soundListController.setMix(mixMap)

            delay(300)
            _mixerState.update {
                it.copy(
                    mixedSounds = mixMap.keys.toList(), isLoading = false
                )
            }
        }
    }

    private fun saveMixToDb(name: String){
        screenModelScope.launch {
            val currentActiveSounds = soundListController.activeSoundsState.value
            val result = saveMixUseCase(name, currentActiveSounds)

            val effectMsg = when(result){
                is SaveMixResult.Success -> {
                    _mixerState.update { it.copy(isSaveDialogVisible = false) }
                    UiText.StringResource(Res.string.msg_mix_saved_success, name)
                }
                else -> UiText.StringResource(Res.string.error_no_sounds)
            }
            _effect.send(MixerEffect.ShowSnackbar(effectMsg))
        }
    }
}