package com.mustafakoceerr.justrelax.core.data.repository

/** Links to the legal pages, in the app's current language. */
interface LegalRepository {
    suspend fun getPrivacyPolicyUrl(): String
    suspend fun getTermsAndConditionsUrl(): String
}
