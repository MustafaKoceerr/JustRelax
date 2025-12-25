package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme {
    SYSTEM, // Cihaz ayarını takip et
    LIGHT,  // Açık tema
    DARK    // Koyu tema
}