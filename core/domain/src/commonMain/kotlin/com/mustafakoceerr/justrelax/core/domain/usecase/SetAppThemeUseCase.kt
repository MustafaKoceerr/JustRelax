package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppTheme

class SetAppThemeUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(theme: AppTheme) {
        repository.setTheme(theme)
    }
}