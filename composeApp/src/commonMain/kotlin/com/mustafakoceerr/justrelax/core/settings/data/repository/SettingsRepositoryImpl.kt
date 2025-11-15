package com.mustafakoceerr.justrelax.core.settings.data.repository

import com.mustafakoceerr.justrelax.core.settings.domain.model.AppLanguage
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings // DİKKAT: Bu import önemli
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set // 'putString' yerine daha basit olan 'set' operatörünü kullanalım
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class SettingsRepositoryImpl (
    private val settings: ObservableSettings
): SettingsRepository{
    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_LANGUAGE = "app_language"
    }

    override suspend fun saveTheme(theme: AppTheme) {
        // 'suspend' olmadığı için suspend fonksiyona gerek yok, ama zararı da yok.
        settings[KEY_THEME] = theme.name
    }

    @OptIn(ExperimentalSettingsApi::class)
    override fun getTheme(): Flow<AppTheme> {
        // 2. ADIM: 'settings' artık doğru tipte olduğu için bu satır çalışacak.
        return settings.getStringFlow(KEY_THEME, defaultValue = AppTheme.SYSTEM.name)
            .map { themeName -> AppTheme.valueOf(themeName) }
    }

    override suspend fun saveLanguage(language: AppLanguage) {
        settings[KEY_LANGUAGE] = language.code
    }

    @OptIn(ExperimentalSettingsApi::class)
    override fun getLanguage(): Flow<AppLanguage> {
        // 3. ADIM: Bu satır da aynı şekilde çalışacak.
        return settings.getStringFlow(KEY_LANGUAGE, defaultValue = AppLanguage.ENGLISH.code)
            .map { code -> AppLanguage.fromCode(code) }
    }
}
