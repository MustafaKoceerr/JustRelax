package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import kotlinx.coroutines.flow.first

class SyncLanguageWithSystemUseCase(
    private val languageSwitcher: LanguageSwitcher,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke() {
        if (languageSwitcher.supportsInAppSwitching) return

        val systemLanguage = languageSwitcher.getSystemLanguage()
        val savedLanguage = userPreferencesRepository.getLanguage().first()

        if (systemLanguage != savedLanguage) {
            userPreferencesRepository.setLanguage(systemLanguage)
        }
    }
}