package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.domain.repository.FileDownloadRepository
import com.mustafakoceerr.justrelax.core.domain.repository.LocalStorageRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

class DownloadSoundUseCase(
    private val soundRepository: SoundRepository,
    private val localStorageRepository: LocalStorageRepository,
    private val fileDownloadRepository: FileDownloadRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(soundId: String): Flow<DownloadStatus> {
        // Geçici dosya yolunu en başta belirle ki finally bloğunda erişebilelim
        var tempPath: String? = null

        return soundRepository.getSound(soundId)
            .flatMapLatest { sound ->
                if (sound == null) {
                    return@flatMapLatest flow { emit(DownloadStatus.Error("Sound not found")) }
                }

                if (sound.isDownloaded) {
                    return@flatMapLatest flow { emit(DownloadStatus.Completed) }
                }

                val soundsDir = localStorageRepository.getSoundsDirectoryPath()
                val extension = sound.remoteUrl.substringAfterLast('.', "m4a")
                val finalPath = "$soundsDir/$soundId.$extension"
                tempPath = "$finalPath.tmp" // tempPath'i burada set et

                // Eski .tmp dosyasını sil (güvenlik)
                if (localStorageRepository.fileExists(tempPath)) {
                    localStorageRepository.deleteFile(tempPath)
                }

                // İndirme akışını başlat
                fileDownloadRepository.downloadFile(sound.remoteUrl, tempPath)
                    .catch { e ->
                        val appError = e as? AppError ?: AppError.Unknown(e)
                        emit(DownloadStatus.Error(appError.message))
                    }
                    .onCompletion { cause ->
                        if (cause == null) { // Başarıyla bittiyse (hata yoksa)
                            localStorageRepository.moveFile(tempPath, finalPath)
                            soundRepository.updateLocalPath(soundId, finalPath)
                        }
                    }
            }
            .cancellable() // İptal edilebilir yap
            .onCompletion {
                // Her durumda (iptal, hata, başarı) .tmp dosyasını temizle
                if (tempPath != null && localStorageRepository.fileExists(tempPath)) {
                    localStorageRepository.deleteFile(tempPath)
                }
            }
    }
}