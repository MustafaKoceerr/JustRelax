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
    suspend operator fun invoke(sound: Sound): Boolean {
        if (sound.isDownloaded) return true

        val soundsDir = localStorageRepository.getSoundsDirectoryPath()
        val extension = sound.remoteUrl.substringAfterLast('.', "m4a").takeIf { it.isNotEmpty() } ?: "m4a"
        val finalPath = "$soundsDir/${sound.id}.$extension"
        val tempPath = "$finalPath.tmp"

        try {
            if (localStorageRepository.fileExists(tempPath)) {
                localStorageRepository.deleteFile(tempPath)
            }
        } catch (e: Exception) { /* Ignored */ }

        val isSuccess = fileDownloadRepository.downloadFile(sound.remoteUrl, tempPath)

        if (isSuccess) {
            try {
                localStorageRepository.moveFile(tempPath, finalPath)
                soundRepository.updateLocalPath(sound.id, finalPath)
                return true
            } catch (e: Exception) {
                return false
            }
        } else {
            try {
                if (localStorageRepository.fileExists(tempPath)) {
                    localStorageRepository.deleteFile(tempPath)
                }
            } catch (e: Exception) { /* Ignored */ }
            return false
        }
    }
}