package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberPlatformColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean // Parametreyi alır ama görmezden gelir
): ColorScheme {
    // iOS'te dinamik renk olmadığı için sadece standart temaları döndürüyoruz.
    return if (darkTheme) DarkColorScheme else LightColorScheme
}