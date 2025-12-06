package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.error_mix_name_empty
import com.mustafakoceerr.justrelax.composeapp.generated.resources.error_mix_name_exists
import com.mustafakoceerr.justrelax.composeapp.generated.resources.error_no_sounds
import com.mustafakoceerr.justrelax.composeapp.generated.resources.msg_mix_saved_success
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.feature.mixer.domain.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerState
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.SaveMixResult
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.SaveMixUseCase
import com.mustafakoceerr.justrelax.utils.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val soundManager: SoundManager,
    // YENİ: Repository eklendi
    private val saveMixUseCase: SaveMixUseCase // UseCase inject edildi
) : ScreenModel {

    private val _state = MutableStateFlow(MixerState())
    val state = _state.asStateFlow()

    private val _effect = Channel<MixerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: MixerIntent) {
        when (intent) {
            is MixerIntent.SelectCount -> {
                _state.update { it.copy(selectedCount = intent.count) }
            }

            is MixerIntent.CreateMix -> createMix()
            is MixerIntent.ShowSaveDialog -> {
                if (_state.value.mixedSounds.isNotEmpty()) {
                    _state.update { it.copy(isSaveDialogVisible = true) }
                }
            }

            is MixerIntent.HideSaveDialog -> {
                _state.update { it.copy(isSaveDialogVisible = false) }
            }

            is MixerIntent.ConfirmSaveMix -> saveMixToDb(intent.name)

            is MixerIntent.ClickDownloadSuggestion -> {
                screenModelScope.launch {
                    // Buraya ileride Analytics kodu ekleyebilirsin:
                    // analytics.logEvent("click_download_suggestion")

                    _effect.send(MixerEffect.NavigateToHome)
                }
            }
        }
    }

    private fun createMix() {
        if (_state.value.isLoading) return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // 1. UseCase'i çağır (Direkt Map döner)
            val mixMap = generateRandomMixUseCase(_state.value.selectedCount)

            // 2. Player'a gönder
            soundManager.setMix(mixMap)

            // 3. UX Beklemesi (Animasyon görünsün diye)
            delay(500)

            // 4. State Güncelle
            _state.update {
                it.copy(
                    mixedSounds = mixMap.keys.toList(),
                    isLoading = false
                )
            }
        }
    }

    private fun saveMixToDb(name: String) {
        screenModelScope.launch {
            val currentActiveSounds = soundManager.state.value.activeSounds
            val result = saveMixUseCase(name, currentActiveSounds)

            when (result) {
                is SaveMixResult.Success -> {
                    _state.update { it.copy(isSaveDialogVisible = false) }

                    // KULLANIM: Resource ID + Parametre (name)
                    _effect.send(
                        MixerEffect.ShowSnackbar(
                            UiText.StringResource(Res.string.msg_mix_saved_success, name)
                        )
                    )
                }

                is SaveMixResult.Error.NameAlreadyExists -> {
                    // KULLANIM: Sadece Resource ID
                    _effect.send(
                        MixerEffect.ShowSnackbar(
                            UiText.StringResource(Res.string.error_mix_name_exists)
                        )
                    )
                }

                is SaveMixResult.Error.EmptyName -> {
                    _effect.send(
                        MixerEffect.ShowSnackbar(
                            UiText.StringResource(Res.string.error_mix_name_empty)
                        )
                    )
                }

                is SaveMixResult.Error.NoSounds -> {
                    _state.update { it.copy(isSaveDialogVisible = false) }
                    _effect.send(
                        MixerEffect.ShowSnackbar(
                            UiText.StringResource(Res.string.error_no_sounds)
                        )
                    )
                }
            }
        }
    }
}