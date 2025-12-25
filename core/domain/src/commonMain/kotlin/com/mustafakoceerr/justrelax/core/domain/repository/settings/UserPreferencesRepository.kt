package com.mustafakoceerr.justrelax.core.domain.repository.settings

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.Flow

/**
 * Sorumluluk: Sadece kullanıcının "Keyif" ve "Tercih" ayarlarını yönetmek.
 * Uygulamanın çalışması için kritik olmayan, özelleştirme verileri.
 */
interface UserPreferencesRepository {

    // --- Tema Tercihi ---
    fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    // --- Dil Tercihi ---
    fun getLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}