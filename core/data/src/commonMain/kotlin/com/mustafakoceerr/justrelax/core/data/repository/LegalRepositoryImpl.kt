package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.network.source.LegalUrlDataSource
import kotlinx.coroutines.flow.first

internal class LegalRepositoryImpl(
    private val preferences: UserPreferencesRepository,
    private val legalUrls: LegalUrlDataSource,
) : LegalRepository {

    override suspend fun getPrivacyPolicyUrl(): String =
        legalUrls.getPrivacyPolicyUrl(currentLanguageCode())

    override suspend fun getTermsAndConditionsUrl(): String =
        legalUrls.getTermsAndConditionsUrl(currentLanguageCode())

    private suspend fun currentLanguageCode() = preferences.getLanguage().first().code
}
