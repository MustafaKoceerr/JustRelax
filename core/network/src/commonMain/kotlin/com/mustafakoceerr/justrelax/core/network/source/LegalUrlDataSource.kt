package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.network.util.RemoteEndpoints

/** The legal pages hosted on the CDN, one per language. */
interface LegalUrlDataSource {
    fun getPrivacyPolicyUrl(languageCode: String): String
    fun getTermsAndConditionsUrl(languageCode: String): String
}

internal class CdnLegalUrlDataSource : LegalUrlDataSource {

    override fun getPrivacyPolicyUrl(languageCode: String): String =
        RemoteEndpoints.getPrivacyPolicyUrl(languageCode)

    override fun getTermsAndConditionsUrl(languageCode: String): String =
        RemoteEndpoints.getTermsUrl(languageCode)
}
