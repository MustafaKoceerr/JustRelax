package com.mustafakoceerr.justrelax.core.domain.repository.system

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

/*
Bu arayüzün değişmesi için tek sebep: İnternetten dosya çekme yöntemimizin değişmesi. (Örn: HTTP yerine FTP kullanmak, veya indirme kütüphanesini değiştirmek).

 */
/**
 * Sorumluluk: Uzak bir sunucudan veriyi bayt bayt çekip yerel bir yola yazmak.
 * Bu repository dosyanın ne olduğuyla (Ses mi, Resim mi) ilgilenmez.
 * Sadece "Transfer" işini yapar.
 */
interface FileDownloadRepository {
    /**
     * Dosyayı indirir ve diske yazar.
     * @return Başarılıysa true, hata olursa false döner.
     * Hata detayını loglar ama akışı bozmamak için basit boolean döner.
     */
    suspend fun downloadFile(url: String, destinationPath: String): Boolean
}