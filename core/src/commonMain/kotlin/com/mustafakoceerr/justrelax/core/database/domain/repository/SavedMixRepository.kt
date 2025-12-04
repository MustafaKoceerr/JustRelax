package com.mustafakoceerr.justrelax.core.database.domain.repository

import com.mustafakoceerr.justrelax.core.database.domain.model.SavedMix
import kotlinx.coroutines.flow.Flow

interface SavedMixRepository {
    // Veritabanındaki değişiklikleri anlık dinlemek için Flow kullanıyoruz
    fun getAllMixes(): Flow<List<SavedMix>>

    // Kaydetme işlemi (SoundManager'dan gelen Map<ID, Volume> yapısını alır)
    suspend fun saveMix(name: String, sounds: Map<String, Float>)

    // Silme işlemi
    suspend fun deleteMix(id: Long)

    suspend fun isMixNameExists(name: String): Boolean
}