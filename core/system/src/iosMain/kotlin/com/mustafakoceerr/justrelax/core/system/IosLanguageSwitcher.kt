package com.mustafakoceerr.justrelax.core.system

import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

internal class IosLanguageSwitcher : LanguageSwitcher {

    override val supportsInAppSwitching: Boolean = false

    override suspend fun updateLanguage(language: AppLanguage) {
        // iOS does not support programmatic language switching within the app.
        // Users are redirected to System Settings via SystemLauncher.
    }

    override fun getSystemLanguage(): AppLanguage {
        val code = NSLocale.currentLocale.languageCode
        return AppLanguage.fromCode(code)
    }
}