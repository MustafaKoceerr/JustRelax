package com.mustafakoceerr.justrelax.core.domain.repository

import com.mustafakoceerr.justrelax.core.model.Sound
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

    // YENİ
    suspend fun syncSounds()

    // YENİ: Sadece indirilmiş sesleri getirir
    fun getDownloadedSounds(): Flow<List<Sound>>

    // YENİ: Sadece indirilmemiş (eksik) sesleri getirir.
    // Flow değil suspend kullanıyoruz çünkü o anlık duruma göre indirme başlatacağız.
    suspend fun getMissingSounds(): List<Sound>
}