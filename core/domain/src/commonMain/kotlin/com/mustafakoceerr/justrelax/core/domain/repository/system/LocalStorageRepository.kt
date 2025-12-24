package com.mustafakoceerr.justrelax.core.domain.repository.system

/*
Bu arayüzün değişmesi için tek sebep: Dosya sistemi üzerindeki manipülasyon kurallarının değişmesi. (Örn: Dosyaları artık Cache klasörüne değil, Documents klasörüne kaydetmek istememiz).

 */

/**
 * Sorumluluk: Cihazın diskindeki dosyaları yönetmek (CRUD).
 * İndirme işlemi yapmaz, sadece var olan dosyalarla ilgilenir.
 */
interface LocalStorageRepository {

    /**
     * Belirtilen yolda bir dosya var mı ve boyutu > 0 mı?
     */
    suspend fun fileExists(path: String): Boolean

    /**
     * Belirtilen dosya veya klasörü siler.
     */
    suspend fun deleteFile(path: String)

    /**
     * Uygulamanın ses dosyalarını saklayacağı ana klasörün yolunu verir.
     * (Platforma göre değişir: Android vs iOS)
     */
    fun getSoundsDirectoryPath(): String

    /**
     * YENİ: Bir dosyayı bir yerden başka bir yere atomik olarak taşır/yeniden adlandırır.
     */
    suspend fun moveFile(sourcePath: String, destinationPath: String)
}
