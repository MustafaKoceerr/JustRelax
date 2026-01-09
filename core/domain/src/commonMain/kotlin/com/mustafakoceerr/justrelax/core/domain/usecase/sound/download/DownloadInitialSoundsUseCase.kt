package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class DownloadInitialSoundsUseCase(
    private val syncSoundsUseCase: SyncSoundsUseCase,
    private val soundRepository: SoundRepository,
    private val downloadBatchSoundsUseCase: DownloadBatchSoundsUseCase
) {
    operator fun invoke(): Flow<DownloadStatus> = flow {
        val syncResult = syncSoundsUseCase()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message ?: "Sync failed"))
            return@flow
        }

        val allSounds = soundRepository.getSounds().first()
        val initialSoundsToDownload = allSounds.filter { it.isInitial && !it.isDownloaded }

        if (initialSoundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        emitAll(downloadBatchSoundsUseCase(initialSoundsToDownload))
    }
}