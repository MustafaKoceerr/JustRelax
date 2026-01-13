package com.mustafakoceerr.justrelax.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatcherProvider,
    private val languageController: LanguageController
) : UserPreferencesRepository {

    override fun getTheme(): Flow<AppTheme> {
        return dataStore.data.map { preferences ->
            val themeName = preferences[DataConstants.KEY_APP_THEME] ?: AppTheme.SYSTEM.name
            AppTheme.valueOf(themeName)
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences[DataConstants.KEY_APP_THEME] = theme.name
            }
        }
    }

    override fun getLanguage(): Flow<AppLanguage> {
        return languageController.currentLanguage
    }

    override suspend fun setLanguage(language: AppLanguage) {
        languageController.setLanguage(language)
    }
}