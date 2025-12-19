package com.mustafakoceerr.justrelax.core.domain.repository

/*
Bu arayüzün değişmesi için tek sebep: Sıkıştırma/Açma algoritmasının veya formatının değişmesi. (Örn: Zip yerine Tar.gz kullanmaya karar vermemiz).
Bu özellik, "Starter Pack" senaryosu için kritiktir.
 */
/**
 * Sorumluluk: Sıkıştırılmış dosyaları (Zip) işlemek.
 */
interface ArchiveRepository {

    /**
     * Verilen Zip dosyasını, hedef klasöre açar.
     * @param zipFilePath: İndirilmiş zip dosyasının yolu.
     * @param destinationDir: İçindekilerin çıkarılacağı klasör.
     */
    suspend fun unzipFile(zipFilePath: String, destinationDir: String): Result<Unit>
}