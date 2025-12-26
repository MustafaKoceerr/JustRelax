package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class DownloadAllSoundsUseCase(
    private val syncSoundsUseCase: SyncSoundsUseCase,
    private val soundRepository: SoundRepository,
    private val downloadBatchSoundsUseCase: DownloadBatchSoundsUseCase
) {
    operator fun invoke(): Flow<DownloadStatus> = flow {
        // 1. Önce sunucuyla senkronize ol (Metadata güncelle)
        val syncResult = syncSoundsUseCase()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message ?: "Sync failed"))
            return@flow
        }

        // 2. Veritabanından tüm sesleri çek
        val allSounds = soundRepository.getSounds().first()

        // 3. Sadece indirilmemiş olanları filtrele
        val soundsToDownload = allSounds.filter { !it.isDownloaded }

        if (soundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        // 4. İşi "Beyin"e (Batch Engine) devret
        emitAll(downloadBatchSoundsUseCase(soundsToDownload))
    }
}