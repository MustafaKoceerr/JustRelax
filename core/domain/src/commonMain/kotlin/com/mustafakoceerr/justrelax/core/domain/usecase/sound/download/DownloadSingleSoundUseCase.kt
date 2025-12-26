package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.repository.system.FileDownloadRepository
import com.mustafakoceerr.justrelax.core.domain.repository.system.LocalStorageRepository
import com.mustafakoceerr.justrelax.core.model.Sound

class DownloadSingleSoundUseCase(
    private val localStorageRepository: LocalStorageRepository,
    private val fileDownloadRepository: FileDownloadRepository,
    private val soundRepository: SoundRepository
) {
    /**
     * Tek bir sesi indirir.
     * @return İşlem başarılıysa (veya zaten inmişse) true, başarısızsa false.
     */
    suspend operator fun invoke(sound: Sound): Boolean {
        // 1. Zaten indirilmişse tekrar uğraşma (Idempotency)
        if (sound.isDownloaded) return true

        val soundsDir = localStorageRepository.getSoundsDirectoryPath()
        // Uzantı kontrolü (yoksa varsayılan m4a)
        val extension = sound.remoteUrl.substringAfterLast('.', "m4a").takeIf { it.isNotEmpty() } ?: "m4a"

        val finalPath = "$soundsDir/${sound.id}.$extension"
        val tempPath = "$finalPath.tmp"

        // 2. Temizlik: Eski yarım kalmış dosya varsa sil
        try {
            if (localStorageRepository.fileExists(tempPath)) {
                localStorageRepository.deleteFile(tempPath)
            }
        } catch (e: Exception) { /* Yut, kritik değil */ }

        // 3. İndirmeyi Başlat (Repository artık Boolean dönüyor)
        val isSuccess = fileDownloadRepository.downloadFile(sound.remoteUrl, tempPath)

        if (isSuccess) {
            try {
                // 4. Atomik İşlem: Tmp dosyasını asıl yerine taşı
                localStorageRepository.moveFile(tempPath, finalPath)

                // 5. Veritabanını güncelle
                soundRepository.updateLocalPath(sound.id, finalPath)
                return true
            } catch (e: Exception) {
                // Taşıma veya DB hatası
                e.printStackTrace()
                return false
            }
        } else {
            // İndirme başarısız olduysa çöp dosya kalmasın
            try {
                if (localStorageRepository.fileExists(tempPath)) {
                    localStorageRepository.deleteFile(tempPath)
                }
            } catch (e: Exception) { /* Yut */ }

            return false
        }
    }
}