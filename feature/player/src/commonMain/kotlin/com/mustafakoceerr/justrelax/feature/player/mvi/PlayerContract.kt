package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.core.model.Sound

// 1. STATE
data class PlayerState(
    // UI'da ikonlarını göstereceğimiz aktif ses nesneleri
    val activeSounds: List<Sound> = emptyList(),

    // Master Play/Pause durumu.
    // (Not: AudioMixer'da 'isPaused' akışı olmadığı için bunu ViewModel'de yerel yöneteceğiz)
    val isPaused: Boolean = false
) {
    // Helper: Player Bar görünmeli mi?
    val isVisible: Boolean
        get() = activeSounds.isNotEmpty()
}

// 2. INTENT
sealed interface PlayerIntent {
    // Play/Pause (Toggle)
    data object ToggleMasterPlayPause : PlayerIntent

    // Hepsini Durdur (Stop All / X Butonu)
    data object StopAll : PlayerIntent
}
