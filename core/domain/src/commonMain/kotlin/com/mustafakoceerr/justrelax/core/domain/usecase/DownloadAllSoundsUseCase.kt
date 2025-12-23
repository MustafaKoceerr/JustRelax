package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

/**
 * Sorumluluk: Önce sunucu ile senkronize olup, ardından veritabanındaki
 * indirilmemiş TÜM sesleri indirme sürecini yönetmek.
 */
class DownloadAllSoundsUseCase(
    private val syncSoundsUseCase: SyncSoundsUseCase,
    private val soundRepository: SoundRepository,
    private val downloadSoundUseCase: DownloadSoundUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DownloadStatus> = flow {
        // 1. ADIM: Sunucu ile senkronize ol (Force Sync)
        // Bu, veritabanının en güncel hali almasını garanti eder.
        val syncResult = syncSoundsUseCase()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message))
            return@flow
        }

        // 2. ADIM: Güncel veritabanından indirilmemiş sesleri oku
        val allSounds = soundRepository.getSounds().first()
        val soundsToDownload = allSounds.filter { !it.isDownloaded }

        // Eğer indirilecek ses yoksa, işlemi tamamla.
        if (soundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        // 3. ADIM: Her bir sesi indirmek için ayrı bir akış (Flow) oluştur
        // ve bunları 'flatMapMerge' ile paralel olarak çalıştır.
        val downloadFlows = soundsToDownload.map { sound ->
            downloadSoundUseCase(sound.id)
        }

        // Tüm indirme akışlarını birleştir ve her birinden gelen durumu yayınla.
        // Not: Toplu ilerleme yüzdesi göstermek için bu kısım daha da geliştirilebilir,
        // ancak şimdilik her dosyanın kendi durumunu yayınlaması yeterlidir.
        downloadFlows.asFlow()
            .flatMapMerge { it }
            .collect { status ->
                emit(status)
            }
    }
}