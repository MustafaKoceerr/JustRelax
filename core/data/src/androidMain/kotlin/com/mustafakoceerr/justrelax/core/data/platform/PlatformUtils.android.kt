package com.mustafakoceerr.justrelax.core.data.platform

actual fun getSystemLanguageCode(): String {
    return java.util.Locale.getDefault().language
}