package com.mustafakoceerr.justrelax.core.domain.repository

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.Flow

/*
Bu arayüzün değişmesi için tek bir sebep olabilir: Kullanıcıya sunduğumuz kişiselleştirme seçeneklerinin değişmesi. (Örn: Font büyüklüğü eklenirse burası değişir).

 */
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