package com.mustafakoceerr.justrelax.core.data.download

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

/** Downloads sound files into the sounds directory, through a temp file so a partial file is never used. */
internal class SoundDownloader(
    private val storage: SoundFileStorage,
    private val fileDownloader: FileDownloader,
) {
    private val semaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)

    /** Returns the local path of the downloaded file, or null if the download failed. */
    suspend fun download(soundId: String, remoteUrl: String): String? {
        val finalPath = "${storage.getSoundsDirectoryPath()}/$soundId.${fileExtensionOf(remoteUrl)}"
        val tempPath = "$finalPath.tmp"

        if (storage.fileExists(finalPath)) return finalPath

        deleteQuietly(tempPath)

        if (!fileDownloader.downloadFile(remoteUrl, tempPath)) {
            deleteQuietly(tempPath)
            return null
        }

        return try {
            storage.moveFile(tempPath, finalPath)
            finalPath
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Downloads [sounds] at most [MAX_PARALLEL_DOWNLOADS] at a time and calls [onDownloaded] for each
     * success. One failure does not stop the others; when all are done the flow ends with
     * [DownloadStatus.Completed], or [DownloadStatus.Error] if at least one failed.
     */
    fun downloadAll(
        sounds: List<Sound>,
        onDownloaded: suspend (sound: Sound, localPath: String) -> Unit,
    ): Flow<DownloadStatus> {
        val totalCount = sounds.size
        if (totalCount == 0) return flowOf(DownloadStatus.Completed)

        return channelFlow {
            val counterLock = Mutex()
            var finishedCount = 0
            var failedCount = 0

            send(DownloadStatus.Progress(0f))

            val jobs = sounds.map { sound ->
                launch {
                    val success = semaphore.withPermit { downloadSafely(sound, onDownloaded) }
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

    private suspend fun downloadSafely(
        sound: Sound,
        onDownloaded: suspend (Sound, String) -> Unit,
    ): Boolean = try {
        val path = download(sound.id, sound.remoteUrl)
        if (path != null) onDownloaded(sound, path)
        path != null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    private suspend fun deleteQuietly(path: String) {
        try {
            if (storage.fileExists(path)) storage.deleteFile(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // A leftover temp file is overwritten by the next download.
        }
    }

    private companion object {
        const val MAX_PARALLEL_DOWNLOADS = 3
        const val DEFAULT_EXTENSION = "m4a"

        /** The extension of the URL's last path segment; query and fragment are ignored. */
        fun fileExtensionOf(url: String): String {
            val lastSegment = url.substringBefore('?').substringBefore('#').substringAfterLast('/')
            return lastSegment.substringAfterLast('.', missingDelimiterValue = "")
                .takeIf { it.isNotEmpty() && it.all(Char::isLetterOrDigit) }
                ?: DEFAULT_EXTENSION
        }
    }
}
