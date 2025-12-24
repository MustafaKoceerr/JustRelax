package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.repository.system.FileDownloadRepository
import com.mustafakoceerr.justrelax.core.domain.repository.system.LocalStorageRepository
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class DownloadSoundUseCase(
    private val soundRepository: SoundRepository,
    private val localStorageRepository: LocalStorageRepository,
    private val fileDownloadRepository: FileDownloadRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(soundId: String): Flow<DownloadStatus> {
        var tempPath: String? = null

        return soundRepository.getSound(soundId)
            .flatMapLatest { sound ->
                if (sound == null) return@flatMapLatest flowOf(DownloadStatus.Error("Sound not found"))
                if (sound.isDownloaded) return@flatMapLatest flowOf(DownloadStatus.Completed)

                val soundsDir = localStorageRepository.getSoundsDirectoryPath()
                val extension = sound.remoteUrl.substringAfterLast('.', "m4a")
                val finalPath = "$soundsDir/$soundId.$extension"
                tempPath = "$finalPath.tmp"

                // Eski bir .tmp dosyası kalmışsa silmeyi dene.
                // Eğer dosya kilitliyse veya silerken "bulunamadı" hatası verirse
                // uygulamayı çökertme, yut ve devam et. Çünkü amacımız zaten üzerine yazmak.
                try {
                    if (localStorageRepository.fileExists(tempPath)) {
                        localStorageRepository.deleteFile(tempPath)
                    }
                } catch (e: Exception) {
                    // Hata yutuldu: Dosya zaten yokmuş veya silinemedi, sorun değil.
                }
                // --- GÜVENLİ TEMİZLİK BİTİŞİ ---

                fileDownloadRepository.downloadFile(sound.remoteUrl, tempPath)
                    .transform { status ->
                        if (status is DownloadStatus.Completed) {
                            // 1. Dosyayı taşı
                            localStorageRepository.moveFile(tempPath, finalPath)
                            // 2. Veritabanını GÜNCELLE
                            soundRepository.updateLocalPath(soundId, finalPath)
                            // 3. Tamamlandı sinyali
                            emit(DownloadStatus.Completed)
                        } else {
                            emit(status)
                        }
                    }
            }
            .cancellable()
            .catch { e ->
                val appError = e as? AppError ?: AppError.Unknown(e)
                emit(DownloadStatus.Error(appError.message))
            }
            .onCompletion {
                // Akış bittiğinde (Hata veya İptal durumunda) tmp dosyası kaldıysa temizle.
                // Burayı da try-catch içine alıyoruz ki kapanışta çökmesin.
                try {
                    if (tempPath != null && localStorageRepository.fileExists(tempPath)) {
                        localStorageRepository.deleteFile(tempPath)
                    }
                } catch (e: Exception) {
                    // Hata yutuldu.
                }
            }
    }
}