package com.mustafakoceerr.justrelax.feature.mixer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.feature.mixer.domain.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val soundManager: SoundManager
): ScreenModel {

    private val _state = MutableStateFlow(MixerState())
    val state = _state.asStateFlow()

    private val _effect = Channel<MixerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: MixerIntent){
        when (intent) {
            is MixerIntent.SelectCount -> {
                _state.update { it.copy(selectedCount = intent.count) }
            }
            is MixerIntent.CreateMix -> createMix()
            is MixerIntent.SaveMix -> { /* Kaydetme işlemi */ }
        }
    }

    private fun createMix(){
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // 1. Önceki sesleri tamamen durdur (Temiz başlangıç)
            soundManager.stopAll()

            // 2. UseCase'den rastgele sesleri ve volumelerini al
            val mixMap = generateRandomMixUseCase(_state.value.selectedCount)

            soundManager.setMix(mixMap)

            // 4. UI State güncelle (Sadece hangi seslerin seçildiğini göstermek için listeyi alıyoruz)
            _state.update {
                it.copy(
                    mixedSounds = mixMap.keys.toList(),
                    isLoading = false
                )
            }
        }
    }
}