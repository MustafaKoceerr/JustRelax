package com.mustafakoceerr.justrelax.core.ui.extensions

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.compositionlocal.LocalLanguageCode

/**
 * Sound nesnesi için Composable bir extension.
 * CompositionLocal'dan o anki dil kodunu okur ve doğru ismi döndürür.
 *
 * Kullanım: val name = sound.displayName()
 */
@Composable
fun Sound.displayName(): String {
    // 1. Havadaki (CompositionLocal) dil kodunu oku
    val languageCode = LocalLanguageCode.current

    // 2. Modeldeki yardımcı fonksiyonu çağır
    return this.getDisplayName(languageCode)
}