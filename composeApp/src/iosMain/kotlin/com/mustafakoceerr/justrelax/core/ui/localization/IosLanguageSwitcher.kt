package com.mustafakoceerr.justrelax.core.ui.localization

import com.mustafakoceerr.justrelax.core.settings.domain.model.AppLanguage
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosLanguageSwitcher : LanguageSwitcher {
    override suspend fun updateLanguage(language: AppLanguage) {
        // iOS'te uygulama içi değişim yapmıyoruz, kullanıcıyı ayarlara yönlendiriyoruz.
    }

    override fun openSystemSettings() {
        // 1. URL String'ini alıyoruz (Sabit değer)
        val urlString = UIApplicationOpenSettingsURLString

        // 2. String'den NSURL oluşturuyoruz
        val settingsUrl = NSURL.URLWithString(urlString)

        // 3. URL geçerli mi ve açılabilir mi?
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {

            // 4. Modern açma yöntemi (iOS 10+)
            // Kotlin Native'de 'open' fonksiyonu bazen parametre isimleriyle çağrılmak ister.
            UIApplication.sharedApplication.openURL(
                settingsUrl,
                mapOf<Any?, Any?>(),
                null
            )
        }
    }
}