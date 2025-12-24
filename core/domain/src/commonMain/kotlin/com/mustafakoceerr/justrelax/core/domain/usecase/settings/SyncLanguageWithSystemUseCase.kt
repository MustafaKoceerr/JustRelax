package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import kotlinx.coroutines.flow.first

class SyncLanguageWithSystemUseCase(
    private val languageSwitcher: LanguageSwitcher,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Sadece uygulama içi dil değişiminin desteklenmediği platformlarda çalışır (iOS, Android 13+).
     * Sistem dilini okur ve DataStore'daki değerle farklıysa günceller.
     */
    suspend operator fun invoke() {
        // Eğer uygulama içinden dil değişebiliyorsa (Android < 13), bu UseCase hiçbir şey yapmaz.
        if (languageSwitcher.supportsInAppSwitching) {
            return
        }

        // 1. O anki sistem dilini al
        val systemLanguage = languageSwitcher.getSystemLanguage()

        // 2. Kayıtlı olan dili al
        val savedLanguage = userPreferencesRepository.getLanguage().first()

        // 3. Eğer farklıysa, DataStore'u güncelle
        if (systemLanguage != savedLanguage) {
            userPreferencesRepository.setLanguage(systemLanguage)
        }
    }
}