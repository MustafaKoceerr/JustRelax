package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.domain.repository.legal.LegalRepository
import com.mustafakoceerr.justrelax.core.network.util.RemoteEndpoints

internal class LegalRepositoryImpl : LegalRepository {

    override fun getPrivacyPolicyUrl(languageCode: String): String {
        return RemoteEndpoints.getPrivacyPolicyUrl(languageCode)
    }

    override fun getTermsAndConditionsUrl(languageCode: String): String {
        return RemoteEndpoints.getTermsUrl(languageCode)
    }
}