package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.Flow

class GetAppThemeUseCase(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<AppTheme> {
        return repository.getTheme()
    }
}