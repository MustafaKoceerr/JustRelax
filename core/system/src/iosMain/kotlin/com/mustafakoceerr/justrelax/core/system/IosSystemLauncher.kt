package com.mustafakoceerr.justrelax.core.system

import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosSystemLauncher : SystemLauncher {

    override fun sendFeedbackEmail(to: String, subject: String, body: String) {
        // iOS'ta mailto şeması ile varsayılan mail uygulamasını açıyoruz
        // Boşlukları ve özel karakterleri encode etmek gerekir (basitlik için direkt yazıyorum)
        val urlString = "mailto:$to?subject=$subject&body=$body"
            .replace(" ", "%20")
        openUrl(urlString)
    }

    override fun openStorePage(appId: String?) {
        // iOS'ta App ID zorunludur (örn: id123456789)
        appId?.let {
            openUrl("itms-apps://itunes.apple.com/app/$it")
        }
    }

    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null && UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }

    override fun openAppLanguageSettings() {
        // iOS Ayarlar -> Uygulama Ayarları sayfasına yönlendirir
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }
}