package com.mustafakoceerr.justrelax.core.network.util

import com.mustafakoceerr.justrelax.core.network.BuildConfig

internal object RemoteEndpoints {
    private const val BASE_URL = BuildConfig.CDN_BASE_URL

    const val SOUND_CATALOG = "${BASE_URL}v2/sounds-v2.json"

    fun getPrivacyPolicyUrl(langCode: String): String {
        return "${BASE_URL}legal/$langCode/privacy-policy.html"
    }

    fun getTermsUrl(langCode: String): String {
        return "${BASE_URL}legal/$langCode/terms-conditions.html"
    }
}
