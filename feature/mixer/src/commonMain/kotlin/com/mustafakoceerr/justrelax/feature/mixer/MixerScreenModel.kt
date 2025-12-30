package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerContract
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

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    soundControllerFactory: SoundController.Factory
) : ScreenModel {
    // SoundController'ı bu ViewModel'in yaşam döngüsüne (scope) bağlıyoruz.
// UI, ses durumlarını (playing, volume) buradan dinleyecek.
    val soundController: SoundController = soundControllerFactory.create(screenModelScope)

    // UI State (Sadece Mixer ekranına özel durumlar)
    private val _state = MutableStateFlow(MixerContract.State())
    val state = _state.asStateFlow()

    // Effect (Snackbar vb.)
    private val _effect = Channel<MixerContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: MixerContract.Event) {
        when (event) {
            is MixerContract.Event.SelectSoundCount -> {
                // 2 ile 7 arasında sınırla
                val count = event.count.coerceIn(2, 7)
                _state.update { it.copy(selectedSoundCount = count) }
            }

            is MixerContract.Event.GenerateMix -> {
                generateAndPlayMix()
            }

            // --- SoundController'a Delege Edilen İşler ---
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
        // Zaten bir işlem varsa tekrar basılmasını engelle
        if (_state.value.isGenerating) return

        screenModelScope.launch {
            _state.update { it.copy(isGenerating = true) }

            // 1. Rastgele Mix Oluştur (UseCase)
            val mixMap = generateRandomMixUseCase(_state.value.selectedSoundCount)

            // 2. Controller'ın ses seviyesi hafızasını (cache) güncelle
            // Böylece sesler çalmaya başladığında doğru seviyede başlar.
            val volumeMapForController = mixMap.map { (sound, volume) ->
                sound.id to volume
            }.toMap()
            soundController.setVolumes(volumeMapForController)

            // 3. Ses Motoruna "Bu listeyi çal" emrini ver
            // (Arka planda paralel hazırlık yapılır, UI donmaz)
            setMixUseCase(mixMap)

            // 4. UI'ı güncelle: Yeni ses listesini göster ve kilidi aç
            _state.update {
                it.copy(
                    isGenerating = false,
                    mixedSounds = mixMap.keys.toList()
                )
            }
        }
    }
}