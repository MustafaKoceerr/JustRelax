package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

class DownloadBatchSoundsUseCase(
    private val downloadSingleSoundUseCase: DownloadSingleSoundUseCase
) {
    private val semaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)

    /**
     * Sesleri en fazla [MAX_PARALLEL_DOWNLOADS] paralel indirir. Bir sesin hatası diğerlerini durdurmaz;
     * hepsi bittiğinde en az biri başarısızsa [DownloadStatus.Error], aksi halde [DownloadStatus.Completed] yayılır.
     */
    operator fun invoke(sounds: List<Sound>): Flow<DownloadStatus> {
        val totalCount = sounds.size
        if (totalCount == 0) return flowOf(DownloadStatus.Completed)

        return channelFlow {
            val counterLock = Mutex()
            var finishedCount = 0
            var failedCount = 0

            send(DownloadStatus.Progress(0f))

            val jobs = sounds.map { sound ->
                launch {
                    val success = semaphore.withPermit { downloadSafely(sound) }
                    val progress = counterLock.withLock {
                        finishedCount++
                        if (!success) failedCount++
                        finishedCount.toFloat() / totalCount
                    }
                    if (progress < 1f) send(DownloadStatus.Progress(progress))
                }
            }
            jobs.forEach { it.join() }

            send(
                if (failedCount == 0) DownloadStatus.Completed
                else DownloadStatus.Error("$failedCount of $totalCount sounds could not be downloaded")
            )
        }
    }

    private suspend fun downloadSafely(sound: Sound): Boolean = try {
        downloadSingleSoundUseCase(soundId = sound.id, remoteUrl = sound.remoteUrl)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    private companion object {
        const val MAX_PARALLEL_DOWNLOADS = 3
    }
}
