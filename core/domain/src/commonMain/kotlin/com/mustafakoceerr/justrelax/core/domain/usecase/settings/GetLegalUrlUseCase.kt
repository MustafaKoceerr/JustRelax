package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.legal.LegalRepository
import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class GetLegalUrlUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val legalRepository: LegalRepository
) {
    suspend fun getPrivacyPolicy(): String {
        val currentLanguage = userPreferencesRepository.getLanguage().first()
        return legalRepository.getPrivacyPolicyUrl(currentLanguage.code)
    }

    suspend fun getTermsAndConditions(): String {
        val currentLanguage = userPreferencesRepository.getLanguage().first()
        return legalRepository.getTermsAndConditionsUrl(currentLanguage.code)
    }
}