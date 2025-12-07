package com.mustafakoceerr.justrelax.core.model

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    ENGLISH("en"),
    TURKISH("tr");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}