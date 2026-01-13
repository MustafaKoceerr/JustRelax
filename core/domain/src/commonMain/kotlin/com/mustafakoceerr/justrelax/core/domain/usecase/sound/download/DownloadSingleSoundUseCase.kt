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
    suspend operator fun invoke(soundId: String, remoteUrl: String): Boolean {
        val soundsDir = localStorageRepository.getSoundsDirectoryPath()
        val extension = remoteUrl.substringAfterLast('.', "m4a").takeIf { it.isNotEmpty() } ?: "m4a"
        val finalPath = "$soundsDir/$soundId.$extension"
        val tempPath = "$finalPath.tmp"

        if (localStorageRepository.fileExists(finalPath)) {
            soundRepository.updateLocalPath(soundId, finalPath)
            return true
        }

        try {
            if (localStorageRepository.fileExists(tempPath)) {
                localStorageRepository.deleteFile(tempPath)
            }
        } catch (e: Exception) { /* Ignored */ }

        val isSuccess = fileDownloadRepository.downloadFile(remoteUrl, tempPath)

        if (isSuccess) {
            try {
                localStorageRepository.moveFile(tempPath, finalPath)
                soundRepository.updateLocalPath(soundId, finalPath)
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