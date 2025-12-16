package com.mustafakoceerr.justrelax.core.ui.localization

import com.mustafakoceerr.justrelax.core.model.AppLanguage

interface LanguageSwitcher {
    // Platformun YETENEĞİ: Uygulama içinde dil değiştirebiliyor mu?
    // Android -> true
    // iOS -> false
    val supportsInAppSwitching: Boolean


    suspend fun updateLanguage(language: AppLanguage)

    fun openSystemSettings()
}