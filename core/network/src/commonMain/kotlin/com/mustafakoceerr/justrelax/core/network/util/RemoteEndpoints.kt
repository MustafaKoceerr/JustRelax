package com.mustafakoceerr.justrelax.core.network.util

import com.mustafakoceerr.justrelax.core.network.BuildConfig

/**
 * URL oluşturma mantığını merkezi bir yerde tutuyoruz.
 * 'internal' olduğu için sadece bu modül (core-network) erişebilir.
 */
internal object RemoteEndpoints {
    private const val BASE_URL = BuildConfig.CDN_BASE_URL

    // Ses Config Dosyası: https://cdn.mustafakoceerr.com/config/sounds.json
    val soundsConfig = "${BASE_URL}config/sounds.json"

    // --- YASAL METİNLER ---

    // Örn: https://cdn.mustafakoceerr.com/legal/tr/privacy-policy.html
    fun getPrivacyPolicyUrl(langCode: String): String {
        return "${BASE_URL}legal/$langCode/privacy-policy.html"
    }

    // Örn: https://cdn.mustafakoceerr.com/legal/en/terms-conditions.html
    fun getTermsUrl(langCode: String): String {
        return "${BASE_URL}legal/$langCode/terms-conditions.html"
    }
}