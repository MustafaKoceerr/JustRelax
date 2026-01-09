package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(
    val code: String,
    val nativeName: String
) {
    SYSTEM("system", "System Default"),
    ENGLISH("en", "English"),
    TURKISH("tr", "Türkçe"),
    SPANISH("es", "Español");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}