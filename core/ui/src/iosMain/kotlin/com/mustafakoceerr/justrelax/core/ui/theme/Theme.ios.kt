package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberPlatformColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme {
    return if (darkTheme) DarkColorScheme else LightColorScheme
}