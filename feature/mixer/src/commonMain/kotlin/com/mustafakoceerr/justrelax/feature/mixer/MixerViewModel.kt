package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val soundManager: SoundManager,
    private val saveMixUseCase: SaveMixUseCase,
    private val toggleSoundUseCase: ToggleSoundUseCase, // <--- ARTIK BU VAR
    private val getDownloadedSoundCountUseCase: GetDownloadedSoundCountUseCase // <--- REPO YERİNE BU VAR
     ) : ScreenModel {

    // İç State
    private val _mixerState = MutableStateFlow(MixerState())

    // Dış State (SoundManager ile birleştirilmiş)
    val state = combine(
        _mixerState,
        soundManager.state
    ) { mixerState, audioState ->
        mixerState.copy(
            activeSounds = audioState.activeSounds.mapValues { it.value.targetVolume }
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MixerState()
    )

    private val _effect = Channel<MixerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // ARTIK REPOSITORY YOK, USECASE VAR
        screenModelScope.launch {
            getDownloadedSoundCountUseCase().collectLatest { count ->
                _mixerState.update {
                    it.copy(showDownloadSuggestion = count < 20)
                }
            }
        }
    }

    fun processIntent(intent: MixerIntent) {
        when (intent) {
            is MixerIntent.SelectCount -> {
                _mixerState.update { it.copy(selectedCount = intent.count) }
            }

            is MixerIntent.CreateMix -> createMix()

            is MixerIntent.ShowSaveDialog -> {
                // Sadece çalan ses varsa kaydetmeye izin ver
                if (soundManager.state.value.activeSounds.isNotEmpty()) {
                    _mixerState.update { it.copy(isSaveDialogVisible = true) }
                } else {
                    screenModelScope.launch {
                        _effect.send(MixerEffect.ShowSnackbar(UiText.StringResource(Res.string.error_no_sounds)))
                    }
                }
            }

            is MixerIntent.HideSaveDialog -> {
                _mixerState.update { it.copy(isSaveDialogVisible = false) }
            }

            is MixerIntent.ConfirmSaveMix -> saveMixToDb(intent.name)

            is MixerIntent.ClickDownloadSuggestion -> {
                screenModelScope.launch {
                    _effect.send(MixerEffect.NavigateToHome)
                }
            }

            is MixerIntent.ToggleSound -> toggleSound(intent.sound)
            is MixerIntent.ChangeVolume -> changeVolume(intent.id, intent.volume)
        }
    }

    private fun toggleSound(sound: Sound) {
        screenModelScope.launch {
            // BEST PRACTICE: Logic tek bir yerde (UseCase)
            // Mixer'da indirme olmadığı için isDownloading hep false
            toggleSoundUseCase(sound, isCurrentlyDownloading = false).collect {
                // Sonuçları dinlememize gerek yok, SoundManager state'i güncelleyince UI güncellenir.
            }
        }
    }

    private fun changeVolume(id: String, volume: Float) {
        soundManager.onVolumeChange(id, volume)
    }

    private fun createMix() {
        if (_mixerState.value.isLoading) return

        screenModelScope.launch {
            _mixerState.update { it.copy(isLoading = true) }

            // 1. Rastgele mix oluştur (Map<Sound, Float>)
            val mixMap = generateRandomMixUseCase(_mixerState.value.selectedCount)

            // 2. SoundManager'a "Bunları çal" de
            soundManager.setMix(mixMap)

            // Biraz bekle ki kullanıcı yükleniyor efektini görsün (Opsiyonel)
            delay(300)

            // 3. UI'ı güncelle (Kartları göster)
            _mixerState.update {
                it.copy(
                    mixedSounds = mixMap.keys.toList(),
                    isLoading = false
                )
            }
        }
    }

    private fun saveMixToDb(name: String) {
        screenModelScope.launch {
            // SoundManager'dan o an aktif olan sesleri alıp kaydediyoruz
            // Not: soundManager.state.value.activeSounds -> Map<String, ActiveSound>
            // Bizim UseCase -> Map<String, Float> istiyor. Dönüştürelim.

            val currentActiveSounds = soundManager.state.value.activeSounds.mapValues {
                it.value.targetVolume
            }

            val result = saveMixUseCase(name, currentActiveSounds)

            when (result) {
                is SaveMixResult.Success -> {
                    _mixerState.update { it.copy(isSaveDialogVisible = false) }
                    _effect.send(
                        MixerEffect.ShowSnackbar(
                            UiText.StringResource(Res.string.msg_mix_saved_success, name)
                        )
                    )
                }
                is SaveMixResult.Error.NameAlreadyExists -> {
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
                    _mixerState.update { it.copy(isSaveDialogVisible = false) }
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