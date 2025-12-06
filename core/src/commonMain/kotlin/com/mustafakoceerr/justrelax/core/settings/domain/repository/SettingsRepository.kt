package com.mustafakoceerr.justrelax.core.settings.domain.repository

import com.mustafakoceerr.justrelax.core.settings.domain.model.AppLanguage
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Tema ayarları
    suspend fun saveTheme(theme: AppTheme)
    fun getTheme(): Flow<AppTheme>

    // Dil ayarları (şimdilik sadece
    suspend fun saveLanguage(language: AppLanguage)
    fun getLanguage(): Flow<AppLanguage>

    // YENİ: Seeding durumu
    suspend fun isInitialSeedingDone(): Boolean
    suspend fun setInitialSeedingDone(isDone: Boolean)


    // --- YENİ EKLENENLER (Banner Zamanlaması İçin) ---
    suspend fun getLastDownloadPromptTime(): Long
    suspend fun setLastDownloadPromptTime(timestamp: Long)
}