package com.mustafakoceerr.justrelax.core.domain.repository.savedmix

import kotlinx.coroutines.flow.Flow

interface SavedMixRepository {
    fun getSavedMixes(): Flow<List<SavedMix>>
    suspend fun saveMix(name: String, soundVolumes: Map<String, Float>)
    suspend fun deleteMix(id: Long)
}