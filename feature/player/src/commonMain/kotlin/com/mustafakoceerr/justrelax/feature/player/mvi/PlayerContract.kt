package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.core.model.Sound

// 1. STATE
data class PlayerState(
    val activeSounds: List<Sound> = emptyList(),

    // ARTIK YEREL DEĞİL, MIXER'DAN GELECEK
    val isPlaying: Boolean = false
) {
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
