package com.mustafakoceerr.justrelax.core.system

import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal class IosSystemLauncher : SystemLauncher {

    override fun sendFeedbackEmail(to: String, subject: String, body: String) {
        val urlString = "mailto:$to?subject=$subject&body=$body"
            .replace(" ", "%20")
        openUrl(urlString)
    }

    override fun openStorePage(appId: String?) {
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
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }
}