package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerState
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.err_mix_save_empty_name
import justrelax.feature.mixer.generated.resources.err_mix_save_no_sound
import justrelax.feature.mixer.generated.resources.err_unknown
import justrelax.feature.mixer.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerScreenModel(
    // Mixer'a özel UseCase'ler
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    private val saveCurrentMixUseCase: SaveCurrentMixUseCase, // YENİ: UseCase eklendi
    soundControllerFactory: SoundController.Factory
) : ScreenModel {

    // Kendi yaşam döngümüze (scope) bağlı bir SoundController oluşturuyoruz.
    val soundController: SoundController = soundControllerFactory.create(screenModelScope)

    private val _state = MutableStateFlow(MixerState())
    val state = _state.asStateFlow()

    private val _effect = Channel<MixerEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: MixerIntent) {
        when (intent) {
            is MixerIntent.SelectSoundCount -> {
                val count = intent.count.coerceIn(2, 7)
                _state.update { it.copy(selectedSoundCount = count) }
            }

            is MixerIntent.GenerateMix -> generateAndPlayMix()

            // --- DELEGE EDİLEN GÖREVLER ---
            // Bu intent'ler geldiğinde, ViewModel düşünmez, sadece işi Controller'a paslar.
            is MixerIntent.ToggleSound -> soundController.toggleSound(intent.soundId)
            is MixerIntent.ChangeVolume -> soundController.changeVolume(
                intent.soundId,
                intent.volume
            )

            // --- DIALOG YÖNETİMİ ---
            is MixerIntent.ShowSaveDialog -> _state.update { it.copy(isSaveDialogVisible = true) }
            is MixerIntent.HideSaveDialog -> _state.update { it.copy(isSaveDialogVisible = false) }
            is MixerIntent.SaveCurrentMix -> saveMix(intent.name)
        }
    }

    private fun generateAndPlayMix() {
        if (_state.value.isGenerating) return

        screenModelScope.launch {
            _state.update { it.copy(isGenerating = true) }

            val mixMap = generateRandomMixUseCase(_state.value.selectedSoundCount)

            // ... (SoundController'ı bilgilendirme kısmı aynı)
            val volumeMapForController = mixMap.map { (sound, volume) -> sound.id to volume }.toMap()
            soundController.setVolumes(volumeMapForController)

            // BU ÇAĞRI ARTIK GERÇEKTEN BEKLEYECEK
            setMixUseCase(mixMap)

            // KİLİT ANCAK İŞ BİTTİĞİNDE AÇILACAK
            _state.update {
                it.copy(
                    isGenerating = false,
                    mixedSounds = mixMap.keys.toList()
                )
            }
        }
    }
    private fun saveMix(name: String) {
        screenModelScope.launch {
            saveCurrentMixUseCase(name, soundController).collect { result ->
                when (result) {
                    is Resource.Loading -> { /* Loading... */ }

                    is Resource.Success -> {
                        _state.update { it.copy(isSaveDialogVisible = false) }

                        sendEffect(
                            MixerEffect.ShowSnackbar(
                                // DÜZELTME BURADA:
                                // XML'deki %1$s için 'name' değişkenini veriyoruz.
                                UiText.Resource(
                                    resId = Res.string.msg_mix_saved_success,
                                    formatArgs = listOf(name)
                                )
                            )
                        )
                    }

                    is Resource.Error -> {
                        _state.update { it.copy(isSaveDialogVisible = false) }

                        val errorText = when (result.error) {
                            is AppError.SaveMix.EmptyName -> UiText.Resource(Res.string.err_mix_save_empty_name)
                            is AppError.SaveMix.NoSoundsPlaying -> UiText.Resource(Res.string.err_mix_save_no_sound)
                            else -> UiText.Resource(Res.string.err_unknown)
                        }
                        sendEffect(MixerEffect.ShowSnackbar(errorText))
                    }
                }
            }
        }
    }
    private fun sendEffect(effect: MixerEffect) {
        screenModelScope.launch { _effect.send(effect) }
    }
}