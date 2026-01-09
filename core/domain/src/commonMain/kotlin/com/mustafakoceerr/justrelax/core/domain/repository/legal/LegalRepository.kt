package com.mustafakoceerr.justrelax.core.domain.repository.legal

interface LegalRepository {
    fun getPrivacyPolicyUrl(languageCode: String): String
    fun getTermsAndConditionsUrl(languageCode: String): String
}