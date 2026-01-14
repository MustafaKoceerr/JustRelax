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
    SPANISH("es", "Español", "Spanish"),
    GERMAN("de", "Deutsch", "German"),
    FRENCH("fr", "Français", "French"),
    PORTUGUESE("pt", "Português", "Portuguese"),
    ITALIAN("it", "Italiano", "Italian"),
    DUTCH("nl", "Nederlands", "Dutch"),
    SWEDISH("sv", "Svenska", "Swedish"),
    HINDI("hi", "हिन्दी", "Hindi"),
    JAPANESE("ja", "日本語", "Japanese"),
    KOREAN("ko", "한국어", "Korean");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}