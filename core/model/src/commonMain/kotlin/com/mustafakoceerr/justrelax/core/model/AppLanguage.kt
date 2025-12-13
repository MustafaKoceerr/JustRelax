package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(
    val code: String,
    val nativeName: String? // Nullable yaptık
) {
    // System için null veriyoruz, çünkü bunu UI'da stringResource ile dolduracağız
    SYSTEM("system", null),

    ENGLISH("en", "English"),
    TURKISH("tr", "Türkçe");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}