package com.mustafakoceerr.justrelax.feature.settings.util

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.theme_system
import org.jetbrains.compose.resources.stringResource

// UI tarafında kullanılacak isim
val AppLanguage.nativeName: String
    @Composable
    get() = when (this) {
        // System Default yazısı telefonun diline göre değişmeli (Sistem Varsayılanı / System Default)
        AppLanguage.SYSTEM -> stringResource(Res.string.theme_system)

        // Dil isimleri genelde çevrilmez, kendi dillerinde yazılır.
        AppLanguage.ENGLISH -> "English"
        AppLanguage.TURKISH -> "Türkçe"
    }