package com.mustafakoceerr.justrelax.core.domain.repository.legal

interface LegalRepository {
    /**
     * Verilen dil koduna göre (örn: "tr", "en") Gizlilik Politikası URL'ini döner.
     */
    fun getPrivacyPolicyUrl(languageCode: String): String

    /**
     * Verilen dil koduna göre Şartlar ve Koşullar URL'ini döner.
     */
    fun getTermsAndConditionsUrl(languageCode: String): String
}