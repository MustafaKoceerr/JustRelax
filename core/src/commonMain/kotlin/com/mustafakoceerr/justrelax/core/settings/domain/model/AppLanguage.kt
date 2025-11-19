package com.mustafakoceerr.justrelax.core.settings.domain.model


/**
 * Uygulamada desteklenen dilleri temsil eder.
 * @param code ISO 639-1 standardında dil kodu (örn: "en", "tr").
 * @param nativeName Dilin kendi dilindeki adı (örn: "English", "Türkçe").
 */

enum class AppLanguage(val code: String, val nativeName: String) {
    SYSTEM(code = "system", nativeName = "System Default"), // Yeni Seçenek
    ENGLISH(code = "en", nativeName = "English"),
    TURKISH(code = "tr", nativeName = "Türkçe");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM // Bulamazsa System dön
        }
    }
}