package com.mustafakoceerr.justrelax.core.domain.controller

import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * UI katmanının seslerle etkileşimini yöneten merkezi kontrolcü için sözleşme.
 * Bu sınıf, ViewModel'ler tarafından kullanılır ve sesle ilgili tüm state ve action'ları içerir.
 */

// Controller'ın UI'a sunacağı anlık durum
data class SoundControllerState(
    val playingSoundIds: Set<String> = emptySet(),
    val soundVolumes: Map<String, Float> = emptyMap()
)

interface SoundController {
    // UI'ın dinleyeceği state
    val state: StateFlow<SoundControllerState>

    // UI'dan gelen komutlar
    fun toggleSound(soundId: String)
    fun changeVolume(soundId: String, volume: Float)

    // YENİ: Controller'ın volüm haritasını dışarıdan güncellemek için.
    fun setVolumes(volumes: Map<String, Float>)

    // Bu controller'ı oluşturacak olan fabrika arayüzü
    interface Factory {
        fun create(scope: CoroutineScope): SoundController
    }
}