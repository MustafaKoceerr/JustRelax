package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    fun getLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)

    /** The user agreed that AI mix requests go to OpenAI, which may use them to improve its models. */
    fun aiDataNoticeAccepted(): Flow<Boolean>
    suspend fun acceptAiDataNotice()
}