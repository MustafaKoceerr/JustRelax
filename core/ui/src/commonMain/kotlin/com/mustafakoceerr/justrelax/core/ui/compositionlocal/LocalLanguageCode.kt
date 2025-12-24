package com.mustafakoceerr.justrelax.core.ui.compositionlocal

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Uygulamanın o anki aktif dil kodunu (örn: "tr", "en")
 * Composable ağacının derinliklerine paslamak için kullanılır.
 */
val LocalLanguageCode = staticCompositionLocalOf<String> {
    // Varsayılan değer, eğer sağlanmazsa kullanılır.
    "en"
}