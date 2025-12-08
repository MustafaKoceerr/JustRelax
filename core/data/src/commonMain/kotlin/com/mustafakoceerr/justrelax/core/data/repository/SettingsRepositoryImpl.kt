package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl (
    private val settings: ObservableSettings
): SettingsRepository {
    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_SEEDING_DONE = "initial_seeding_done"

        private const val KEY_LAST_PROMPT = "last_download_prompt_time"
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

    // Todo: Bu bir cold flow, bunu stateFlow ile değiştir ve daha profesyonel yönet.
    @OptIn(ExperimentalSettingsApi::class)
    override fun getLanguage(): Flow<AppLanguage> {
        // 3. ADIM: Bu satır da aynı şekilde çalışacak.
        return settings.getStringFlow(KEY_LANGUAGE, defaultValue = AppLanguage.ENGLISH.code)
            .map { code -> AppLanguage.fromCode(code) }
    }

    override suspend fun isInitialSeedingDone(): Boolean {
        return settings.getBoolean(KEY_SEEDING_DONE, defaultValue = false)
    }

    override suspend fun setInitialSeedingDone(isDone: Boolean) {
        settings[KEY_SEEDING_DONE] = isDone
    }

    // --- YENİ IMPLEMENTASYONLAR ---
    override suspend fun getLastDownloadPromptTime(): Long {
        // Varsayılan olarak 0L döner (Hiç gösterilmedi demek)
        return settings.getLong(KEY_LAST_PROMPT, defaultValue = 0L)
    }
    override suspend fun setLastDownloadPromptTime(timestamp: Long) {
        // settings[KEY] = value syntax'ı Long için de çalışır (library extension sayesinde)
        // veya settings.putLong(KEY_LAST_PROMPT, timestamp) de diyebilirsin.
        settings[KEY_LAST_PROMPT] = timestamp
    }
}