package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerState
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
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
    // TODO: private val saveCurrentMixUseCase: SaveCurrentMixUseCase,

    // Merkezi Ses Kontrolcüsü Fabrikası
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
            is MixerIntent.ToggleSound -> soundController.toggleSound(intent.sound)
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
        // TODO: SaveCurrentMixUseCase implementasyonu
        screenModelScope.launch {
            _state.update { it.copy(isSaveDialogVisible = false) }
            _effect.send(MixerEffect.ShowSnackbar("Save feature is not implemented yet."))
        }
    }

}