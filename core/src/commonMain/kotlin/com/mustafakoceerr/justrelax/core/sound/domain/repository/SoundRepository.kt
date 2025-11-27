package com.mustafakoceerr.justrelax.core.sound.domain.repository

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import kotlinx.coroutines.flow.Flow

interface SoundRepository{
    /**
     * Tüm seslerin listesini akış olarak döndürür.
     * Flow kullanmamızın sebebi, ileride veritabanı değişirse UI'ın anlık güncellenmesidir.
     */
    fun getSounds(): Flow<List<Sound>>

    /**
     * ID'ye göre tek bir sesin detayını getirir.
     */
    suspend fun getSoundById(id: String): Sound?
}