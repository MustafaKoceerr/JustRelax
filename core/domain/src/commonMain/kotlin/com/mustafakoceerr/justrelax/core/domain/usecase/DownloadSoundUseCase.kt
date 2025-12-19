package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.FileDownloadRepository
import com.mustafakoceerr.justrelax.core.domain.repository.LocalStorageRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * Sorumluluk: Belirli bir sesi indirme işlemini baştan sona yönetmek.
 */
class DownloadSoundUseCase(
    private val soundRepository: SoundRepository,
    private val localStorageRepository: LocalStorageRepository,
    private val fileDownloadRepository: FileDownloadRepository
) {
    suspend operator fun invoke(soundId: String): Flow<DownloadStatus> = flow {
        val sound = soundRepository.getSound(soundId).first()
        if (sound == null) {
            emit(DownloadStatus.Error("Sound not found"))
            return@flow
        }
        if (sound.isDownloaded) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        val soundsDir = localStorageRepository.getSoundsDirectoryPath()
        val destinationPath = "$soundsDir/$soundId.mp3"
        val tempPath = "$destinationPath.tmp" // Geçici dosya yolu

        try {
            emit(DownloadStatus.Queued)

            // Başlamadan önce eski bir geçici dosya kalmışsa temizle.
            if (localStorageRepository.fileExists(tempPath)) {
                localStorageRepository.deleteFile(tempPath)
            }

            fileDownloadRepository.downloadFile(sound.remoteUrl, tempPath).collect { status ->
                emit(status)

                if (status is DownloadStatus.Completed) {
                    // İndirme bittiğinde, .tmp dosyasını asıl yerine taşı.
                    localStorageRepository.moveFile(tempPath, destinationPath)
                    // Veritabanını güncelle.
                    soundRepository.updateLocalPath(soundId, destinationPath)
                }
            }
        } catch (e: CancellationException) {
            // Coroutine iptal edilirse, hatayı tekrar fırlat ve cleanup'ı bekle.
            throw e
        } catch (e: Exception) {
            emit(DownloadStatus.Error(e.message ?: "An unknown error occurred."))
        } finally {
            // Hata, iptal veya başarı durumunda, geride .tmp dosyası kalmadığından emin ol.
            if (localStorageRepository.fileExists(tempPath)) {
                localStorageRepository.deleteFile(tempPath)
            }
        }
    }
}

/*
Bu yeni versiyon, artık çok daha sağlam:
İndirmeyi .tmp dosyasına yapıyor.
Sadece başarıyla bitince dosyayı taşıyor.
Hata, iptal veya başarı durumunda arkasında çöp (.tmp dosyası) bırakmıyor.
Hata mesajları artık İngilizce ve genel.
 */