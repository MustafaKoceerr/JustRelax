package com.mustafakoceerr.justrelax.core.domain.repository

import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow

/*
Bu arayüzün değişmesi için tek sebep: Uygulamanın ses verisini gösterme şeklinin veya ihtiyaç duyduğu verinin değişmesi. (Örn: Seslere "Favori" özelliği gelirse burası değişir).

 */
/**
 * Sorumluluk: Sadece var olan ses verilerini SORGULAMAK (Read-Only).
 * Bu repository, verinin nereden geldiğini (Sync mi edildi, elle mi eklendi) bilmez.
 * Sadece "Bana sesleri ver" der.
 */
interface SoundRepository {

    /**
     * Tüm seslerin listesini akış olarak verir.
     */
    fun getSounds(): Flow<List<Sound>>

    /**
     * Belirli bir sesin detayını verir.
     */
    fun getSound(id: String): Flow<Sound?>

    /**
     * YENİ: Belirli bir sesin yerel dosya yolunu günceller.
     * Bu, indirme işlemi tamamlandığında veya dosya silindiğinde kullanılır.
     */
    suspend fun updateLocalPath(soundId: String, localPath: String?)
}