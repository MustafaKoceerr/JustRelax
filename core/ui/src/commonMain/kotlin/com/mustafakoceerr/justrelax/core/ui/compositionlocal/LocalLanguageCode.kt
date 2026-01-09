package com.mustafakoceerr.justrelax.core.ui.compositionlocal

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides the current application language code (e.g., "en", "tr") to the composition tree.
 */
val LocalLanguageCode = staticCompositionLocalOf<String> {
    "en"
}