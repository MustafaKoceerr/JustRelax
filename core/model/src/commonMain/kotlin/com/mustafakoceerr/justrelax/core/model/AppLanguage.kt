package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val aiPromptName: String
) {
    SYSTEM("system", "System Default", "English"),

    ENGLISH("en", "English", "English"),
    TURKISH("tr", "Türkçe", "Turkish"),
    SPANISH("es", "Español", "Spanish");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}