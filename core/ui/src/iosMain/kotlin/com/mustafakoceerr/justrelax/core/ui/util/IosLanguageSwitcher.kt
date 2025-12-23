package com.mustafakoceerr.justrelax.core.ui.util

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

    override fun getCurrentLanguage(): AppLanguage {
        // Cihazın veya uygulamanın o anki aktif dil kodunu alır
        val code = NSLocale.currentLocale.languageCode
        return AppLanguage.fromCode(code)
    }
}