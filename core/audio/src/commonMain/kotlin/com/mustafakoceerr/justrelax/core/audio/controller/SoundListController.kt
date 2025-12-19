package com.mustafakoceerr.justrelax.core.audio.controller

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * Ses listesi mantığını (Logic) kapsüller.
 * ViewModel'ler (AiViewModel, MixerViewModel) bunu delegate olarak kullanır.
 */
class SoundListController(
    private val soundManager: SoundManager,
    private val scope: CoroutineScope
) {

    /**
     * UI'ın ihtiyaç duyduğu basit veri yapısı: Map<SoundId, Volume>
     * Sadece çalan sesler bu map içinde yer alır.
     */
    val activeSoundsState: StateFlow<Map<String, Float>> = soundManager.state
        .map { state ->
            // ActiveSound objesinden sadece volume bilgisini alıyoruz
            state.activeSounds.mapValues { it.value.targetVolume }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /**
     * Bir sese tıklandığında (Play/Pause)
     */
    fun onSoundClicked(sound: Sound) {
        scope.launch {
            soundManager.toggleSound(sound)
        }
    }

    /**
     * Ses seviyesi değiştirildiğinde
     */
    fun onVolumeChanged(soundId: String, newVolume: Float) {
        soundManager.onVolumeChange(soundId, newVolume)
    }

    /**
     * Toplu Mix çalma işlemi (Mixer ve AI için)
     */
    fun setMix(mix: Map<Sound, Float>) {
        scope.launch {
            soundManager.setMix(mix)
        }
    }

    /**
     * FACTORY PATTERN:
     * ViewModel'ler SoundManager'ı bilmek zorunda kalmadan,
     * sadece kendi scope'larını vererek Controller üretebilirler.
     */
    class Factory(private val soundManager: SoundManager) {
        fun create(scope: CoroutineScope): SoundListController {
            return SoundListController(soundManager, scope)
        }
    }
}