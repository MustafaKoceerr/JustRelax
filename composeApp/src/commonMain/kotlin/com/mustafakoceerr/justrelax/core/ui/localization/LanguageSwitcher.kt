package com.mustafakoceerr.justrelax.core.ui.localization

import com.mustafakoceerr.justrelax.core.settings.domain.model.AppLanguage

interface LanguageSwitcher {
    suspend fun updateLanguage(language: AppLanguage)

    // Sistemin uygulama ayarlarını açar (iOS için kritik, Android için opsiyonel)
    fun openSystemSettings() // iOS için gerekli
}