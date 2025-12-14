package com.mustafakoceerr.justrelax.core.data.platform

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getSystemLanguageCode(): String {
    return NSLocale.currentLocale.languageCode ?: "en"
}