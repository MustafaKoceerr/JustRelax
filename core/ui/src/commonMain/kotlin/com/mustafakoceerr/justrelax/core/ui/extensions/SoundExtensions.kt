package com.mustafakoceerr.justrelax.core.ui.extensions

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.compositionlocal.LocalLanguageCode

@Composable
fun Sound.displayName(): String {
    val languageCode = LocalLanguageCode.current
    return this.getDisplayName(languageCode)
}