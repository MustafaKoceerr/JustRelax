package com.mustafakoceerr.justrelax.core.system

import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

class IosLanguageSwitcher : LanguageSwitcher {

    // iOS uygulama içi değişimi desteklemez, ayarlara yönlendiririz.
    override val supportsInAppSwitching: Boolean = false

    override suspend fun updateLanguage(language: AppLanguage) {
        // iOS'ta programatik olarak dil değiştirilemez.
        // Kullanıcı zaten SystemLauncher ile ayarlara yönlendirildiği için burası boş kalır.
    }

    override fun getSystemLanguage(): AppLanguage {
        val code = NSLocale.currentLocale.languageCode
        return AppLanguage.fromCode(code)
    }
}