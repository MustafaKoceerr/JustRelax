package com.mustafakoceerr.justrelax.ui.localization

import com.mustafakoceerr.justrelax.core.model.AppLanguage

interface LanguageSwitcher {
    suspend fun updateLanguage(language: AppLanguage)

    // Sistemin uygulama ayarlarını açar (iOS için kritik, Android için opsiyonel)
    fun openSystemSettings() // iOS için gerekli
}