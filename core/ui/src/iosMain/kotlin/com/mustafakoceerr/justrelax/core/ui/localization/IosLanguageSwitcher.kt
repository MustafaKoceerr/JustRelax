package com.mustafakoceerr.justrelax.core.ui.localization

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosLanguageSwitcher : LanguageSwitcher {
    // iOS uygulama içinde değiştiremez -> FALSE
    override val supportsInAppSwitching: Boolean = false

    override suspend fun updateLanguage(language: AppLanguage) {
        // iOS'te kod ile dil değişmez, boş.
    }

    override fun openSystemSettings() {
        val urlString = UIApplicationOpenSettingsURLString
        val settingsUrl = NSURL.URLWithString(urlString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl, mapOf<Any?, Any?>(), null)
        }
    }
}