package com.mustafakoceerr.justrelax.core.domain.controller

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Mixer ve AI gibi "sadece indirilmiş seslerle" çalışan ekranların
 * ses motoruyla etkileşimini basitleştiren bir arayüz (Facade).
 */
interface SoundController {
    /**
     * UI'ın dinleyeceği tek gerçeklik kaynağı.
     * Artık GlobalMixerState'i doğrudan yansıtıyoruz.
     */
    val state: StateFlow<GlobalMixerState>

    /**
     * Bir sesin çalma durumunu tersine çevirir (Play/Stop).
     * Bu bir suspend fonksiyonudur ve CoroutineScope içinden çağrılmalıdır.
     */
    suspend fun toggleSound(soundId: String)

    /**
     * Bir sesin volümünü anlık olarak değiştirir. Suspend değildir.
     */
    fun changeVolume(soundId: String, volume: Float)

    /**
     * Bir mix'i (preset) yüklerken birden çok sesin volümünü
     * tek seferde ayarlamak için kullanılır.
     */
    fun setVolumes(volumes: Map<String, Float>)

    /**
     * Bu controller'ı, belirli bir CoroutineScope'a (örn: screenModelScope)
     * bağlı olarak oluşturan fabrika arayüzü.
     */
    interface Factory {
        fun create(scope: CoroutineScope): SoundController
    }
}