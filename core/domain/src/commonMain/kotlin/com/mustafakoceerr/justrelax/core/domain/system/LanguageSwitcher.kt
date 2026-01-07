package com.mustafakoceerr.justrelax.core.domain.system

import com.mustafakoceerr.justrelax.core.model.AppLanguage

interface LanguageSwitcher {
    val supportsInAppSwitching: Boolean
    suspend fun updateLanguage(language: AppLanguage)
    fun getSystemLanguage(): AppLanguage
}