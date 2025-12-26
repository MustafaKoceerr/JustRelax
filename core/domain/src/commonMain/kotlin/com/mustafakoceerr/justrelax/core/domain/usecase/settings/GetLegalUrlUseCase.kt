package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.legal.LegalRepository
import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Kullanıcının SEÇTİĞİ dili otomatik olarak algılayıp,
 * o dile uygun yasal metin linkini getiren UseCase.
 */
class GetLegalUrlUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val legalRepository: LegalRepository
) {

    suspend fun getPrivacyPolicy(): String {
        // 1. Kullanıcının seçtiği dili Flow'dan "o anki" değerini alarak okuyoruz.
        val currentLanguage = userPreferencesRepository.getLanguage().first()

        // 2. O dile uygun URL'i istiyoruz.
        return legalRepository.getPrivacyPolicyUrl(currentLanguage.code)
    }

    suspend fun getTermsAndConditions(): String {
        val currentLanguage = userPreferencesRepository.getLanguage().first()
        return legalRepository.getTermsAndConditionsUrl(currentLanguage.code)
    }
}