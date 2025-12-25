package com.mustafakoceerr.justrelax.core.domain.repository.savedmix

import kotlinx.coroutines.flow.Flow
interface SavedMixRepository {

    /**
     * Tüm kayıtlı mix'leri, içindeki ses nesneleriyle eşleştirilmiş halde getirir.
     */
    fun getSavedMixes(): Flow<List<SavedMix>>

    /**
     * Yeni bir mix kaydeder.
     * @param name Mix'in adı.
     * @param soundVolumes Ses ID'si ve Ses Seviyesi eşleşmesi. (Sound nesnesine gerek yok, ID yeterli)
     */
    suspend fun saveMix(name: String, soundVolumes: Map<String, Float>)

    /**
     * ID'si verilen mix'i siler.
     */
    suspend fun deleteMix(id: Long)
}