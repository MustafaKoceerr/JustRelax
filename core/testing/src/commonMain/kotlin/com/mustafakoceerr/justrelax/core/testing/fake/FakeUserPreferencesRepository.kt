package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository : UserPreferencesRepository {

    val theme = MutableStateFlow(AppTheme.SYSTEM)
    val language = MutableStateFlow(AppLanguage.SYSTEM)

    override fun getTheme(): Flow<AppTheme> = theme

    override suspend fun setTheme(theme: AppTheme) {
        this.theme.value = theme
    }

    override fun getLanguage(): Flow<AppLanguage> = language

    override suspend fun setLanguage(language: AppLanguage) {
        this.language.value = language
    }
}
