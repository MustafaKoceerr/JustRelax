package com.mustafakoceerr.justrelax.core.domain.source

import com.mustafakoceerr.justrelax.core.model.Sound

/**
 * Sorumluluk: Uzak sunucudan 'Sound' ile ilgili verileri çekmek için bir sözleşme.
 * Repository katmanı bu arayüze bağımlı olacak, implementasyon detayını bilmeyecek (DIP).
 */
//Sadece Config'leri (Metadata) getirecek.
interface SoundRemoteDataSource {

    /**
     * Sunucudaki tüm seslerin listesini (metadata) getirir.
     * Başarılı olursa DTO listesi döner, hata olursa Ktor kendi exception'ını fırlatır.
     * Bu exception'ı Repository katmanında yakalayıp kendi AppError'ımıza çevireceğiz.
     */
    suspend fun getSounds(): List<Sound>
}