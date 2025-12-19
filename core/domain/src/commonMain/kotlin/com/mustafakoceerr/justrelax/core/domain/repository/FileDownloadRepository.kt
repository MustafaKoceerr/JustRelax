package com.mustafakoceerr.justrelax.core.domain.repository

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
     * Verilen URL'deki dosyayı, belirtilen yerel yola indirir.
     * @param url: İndirilecek dosyanın adresi.
     * @param destinationPath: Cihazda kaydedileceği tam yol.
     */
    fun downloadFile(url: String, destinationPath: String): Flow<DownloadStatus>
}