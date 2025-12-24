package com.mustafakoceerr.justrelax.core.domain.usecase.settings

import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import kotlinx.coroutines.flow.Flow

class GetAppLanguageUseCase(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<AppLanguage> {
        return repository.getLanguage()
    }
}