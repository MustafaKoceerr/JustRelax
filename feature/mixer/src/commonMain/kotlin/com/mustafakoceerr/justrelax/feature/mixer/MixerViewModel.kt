package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerContract
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.err_mixer_generate_failed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    soundControllerFactory: SoundController.Factory
) : ScreenModel {

    val soundController: SoundController = soundControllerFactory.create(screenModelScope)

    private val _state = MutableStateFlow(MixerContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<MixerContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: MixerContract.Event) {
        when (event) {
            is MixerContract.Event.SelectSoundCount -> {
                val count = event.count.coerceIn(2, 7)
                _state.update { it.copy(selectedSoundCount = count) }
            }

            is MixerContract.Event.GenerateMix -> {
                generateAndPlayMix()
            }

            is MixerContract.Event.ToggleSound -> {
                screenModelScope.launch {
                    soundController.toggleSound(event.soundId)
                }
            }

            is MixerContract.Event.ChangeVolume -> {
                soundController.changeVolume(event.soundId, event.volume)
            }
        }
    }

    private fun generateAndPlayMix() {
        if (_state.value.isGenerating) return

        screenModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            try {
                val mixMap = generateRandomMixUseCase(_state.value.selectedSoundCount)

                check(mixMap.isNotEmpty()) { "No sounds available to generate a mix." }

                val volumeMapForController = mixMap.map { (soundUi, volume) ->
                    soundUi.id to volume
                }.toMap()

                soundController.setVolumes(volumeMapForController)
                setMixUseCase(mixMap)

                _state.update {
                    it.copy(
                        isGenerating = false,
                        mixedSounds = mixMap.keys.toList()
                    )
                }
            } catch (e: Exception) {
                sendEffect(MixerContract.Effect.ShowSnackbar(UiText.Resource(Res.string.err_mixer_generate_failed)))
                _state.update { it.copy(isGenerating = false, mixedSounds = emptyList()) }
            }
        }
    }

    private fun sendEffect(effect: MixerContract.Effect) {
        screenModelScope.launch {
            _effect.send(effect)
        }
    }
}