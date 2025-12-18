package com.mustafakoceerr.justrelax.core.audio.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.BatchDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class DownloadAllMissingSoundsUseCase(
    private val repository: SoundRepository,
    private val soundDownloader: SoundDownloader
) {
    operator fun invoke(): Flow<BatchDownloadStatus> = flow {
        // 1. ADIM: Veriyi Çek (Filtreleme işi Repository'de bitti)
        val missingSounds = repository.getMissingSounds()
        val totalMissing = missingSounds.size

        if (totalMissing == 0) {
            emit(BatchDownloadStatus.Completed)
            return@flow
        }

        emit(BatchDownloadStatus.Progress(0f))

        var downloadedCount = 0

        // 2. ADIM: İndirme İşlemi (Delegasyon)
        missingSounds.forEach { sound ->
            val isSuccess = soundDownloader.downloadSound(sound.id)
            if (isSuccess) {
                downloadedCount++
                val progress = downloadedCount.toFloat() / totalMissing.toFloat()
                emit(BatchDownloadStatus.Progress(progress))
            }
        }

        emit(BatchDownloadStatus.Completed)
    }
}