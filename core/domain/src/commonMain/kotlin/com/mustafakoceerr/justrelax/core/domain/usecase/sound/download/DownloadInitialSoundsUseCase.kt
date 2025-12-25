package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsUseCase
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
        // 1. Sync
        val syncResult = syncSoundsUseCase()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message ?: "Sync failed"))
            return@flow
        }

        // 2. Verileri çek
        val allSounds = soundRepository.getSounds().first()

        // 3. Filtrele: Sadece "Initial" işaretli VE "İndirilmemiş" olanlar
        val initialSoundsToDownload = allSounds.filter { it.isInitial && !it.isDownloaded }

        if (initialSoundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        // 4. İşi "Beyin"e devret
        emitAll(downloadBatchSoundsUseCase(initialSoundsToDownload))
    }
}