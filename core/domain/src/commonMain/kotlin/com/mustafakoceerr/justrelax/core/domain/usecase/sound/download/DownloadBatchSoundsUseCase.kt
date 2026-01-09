package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class DownloadBatchSoundsUseCase(
    private val downloadSingleSoundUseCase: DownloadSingleSoundUseCase
) {
    private val semaphore = Semaphore(3)

    operator fun invoke(sounds: List<Sound>): Flow<DownloadStatus> {
        val totalCount = sounds.size
        if (totalCount == 0) return flowOf(DownloadStatus.Completed)

        return channelFlow {
            send(0)
            sounds.forEach { sound ->
                launch {
                    semaphore.withPermit {
                        try {
                            downloadSingleSoundUseCase(sound)
                        } catch (e: Exception) {
                            // Log error if needed
                        } finally {
                            send(1)
                        }
                    }
                }
            }
        }
            .runningFold(0) { finishedCount, signal -> finishedCount + signal }
            .map { finishedCount ->
                val progress = finishedCount.toFloat() / totalCount
                if (progress >= 1.0f) DownloadStatus.Completed else DownloadStatus.Progress(progress)
            }
            .distinctUntilChanged()
    }
}