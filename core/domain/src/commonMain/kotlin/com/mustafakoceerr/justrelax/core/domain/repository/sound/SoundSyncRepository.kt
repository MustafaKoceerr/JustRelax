package com.mustafakoceerr.justrelax.core.domain.repository.sound

import com.mustafakoceerr.justrelax.core.common.Resource

/*
Bu arayüzün değişmesi için tek sebep: Backend (Sunucu) ile iletişim kurallarının değişmesi. (Örn: Artık JSON değil XML kullanıyoruz veya Sync mantığı değişti).

 */
/**
 * Sorumluluk: Yerel veritabanını Sunucu ile senkronize etmek.
 * Bu repository veri okumaz, sadece veriyi güncel tutmakla görevli operasyonları barındırır.
 */
interface SoundSyncRepository {

    /**
     * Sunucudaki metadata (JSON) ile yerel veritabanını karşılaştırır.
     * Yeni sesleri ekler, değişenleri günceller, silinenleri kaldırır.
     */
    suspend fun syncWithServer(): Resource<Unit>
}