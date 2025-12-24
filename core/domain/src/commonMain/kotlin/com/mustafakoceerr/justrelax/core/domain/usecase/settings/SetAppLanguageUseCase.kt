package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage

class SetAppLanguageUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(language: AppLanguage) {
        repository.setLanguage(language)
    }
}