package com.mustafakoceerr.justrelax.core.network.util

import com.mustafakoceerr.justrelax.core.network.BuildConfig

internal object RemoteEndpoints {
    private const val BASE_URL = BuildConfig.CDN_BASE_URL

    val soundsConfig = "${BASE_URL}config/sounds.json"

    fun getPrivacyPolicyUrl(langCode: String): String {
        return "${BASE_URL}legal/$langCode/privacy-policy.html"
    }

    fun getTermsUrl(langCode: String): String {
        return "${BASE_URL}legal/$langCode/terms-conditions.html"
    }
}