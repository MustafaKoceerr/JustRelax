package com.mustafakoceerr.justrelax.core.domain.system

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import kotlinx.coroutines.flow.Flow

enum class LanguageStrategy {
    IN_APP,
    SYSTEM_SETTINGS;
}

interface LanguageController {
    val strategy: LanguageStrategy
    val currentLanguage: Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    fun getCurrentLanguage(): AppLanguage
}