package com.mustafakoceerr.justrelax.core.ui.util

import platform.Foundation.NSURL
import platform.Foundation.NSString
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.UIKit.UIApplication

class IosSystemLauncher : SystemLauncher {

    override fun sendFeedbackEmail(to: String, subject: String, body: String) {
        // 1. Kotlin String -> iOS NSString dönüşümü yapıyoruz (as NSString)
        val encodedSubject = (subject as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: ""

        val encodedBody = (body as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: ""

        val urlString = "mailto:$to?subject=$encodedSubject&body=$encodedBody"
        openUrl(urlString)
    }

    override fun openStorePage(appId: String?) {
        // App ID henüz yoksa varsayılan bir yere veya ana sayfaya yönlendiririz.
        val finalId = appId ?: "id000000000"
        val url = "https://apps.apple.com/app/$finalId"
        openUrl(url)
    }

    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null && UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            // iOS 10+ için modern açma yöntemi
            UIApplication.sharedApplication.openURL(nsUrl, mapOf<Any?, Any?>(), null)
        }
    }
}