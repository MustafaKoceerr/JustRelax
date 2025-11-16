package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme {
    val context = LocalContext.current

    // Dinamik renk isteniyor ve cihaz en az Android 12 (S, API 31) ise
    // Not: Dynamic color istiyorsan aşağıdaki comment bloğunu aç
    /**
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    return if (darkTheme) {
    dynamicDarkColorScheme(context)
    } else {
    dynamicLightColorScheme(context)
    }
    }
     */

    // Aksi durumda kendi statik şemalarına dön
    return if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
}