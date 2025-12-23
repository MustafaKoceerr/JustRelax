package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import kotlinx.coroutines.flow.Flow

class GetAppLanguageUseCase(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<AppLanguage> {
        return repository.getLanguage()
    }
}