package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.core.model.Sound

// 1. STATE: UI ne görüyorsa burada olmalı
data class PlayerState(
    val activeSounds: List<Sound> = emptyList(), // Aktif ses listesi
    val isMasterPlaying: Boolean = false,        // Genel oynatma durumu
    val isLoading: Boolean = false
) {
    // UI kolayca erişsin diye helper
    val isVisible: Boolean
        get() = activeSounds.isNotEmpty()

    // Sadece ikon URL'lerini UI'a vermek için helper
    val activeIconUrls: List<String>
        get() = activeSounds.map { it.iconUrl }
}

// 2. INTENT: Kullanıcının yaptığı eylemler
sealed interface PlayerIntent {
    data object ToggleMasterPlayPause : PlayerIntent // Play/Pause butonuna basıldı
    data object StopAll : PlayerIntent               // Çarpı butonuna basıldı
}

