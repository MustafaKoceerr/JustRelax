package com.mustafakoceerr.justrelax.core.sound.data.manager

import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.sound.domain.model.DownloadStatus
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

class SoundDownloaderImpl(
    private val client: HttpClient,
    private val db: JustRelaxDb,
    private val storageProvider: StoragePathProvider
) : SoundDownloader {

    private val queries = db.justRelaxDbQueries

    // Okio FileSystem (Platform bağımsız dosya sistemi)
    private val fileSystem = FileSystem.SYSTEM
    override fun downloadSoundFlow(soundId: String): Flow<DownloadStatus> = channelFlow {
        send(DownloadStatus.Started)

        try {
            val entity = queries.getSoundById(soundId).executeAsOneOrNull()
            if (entity == null) {
                send(DownloadStatus.Error("Sound not found in DB"))
                return@channelFlow
            }

            // Zaten indirilmişse
            if (entity.localPath != null && fileSystem.exists(entity.localPath.toPath())) {
                send(DownloadStatus.Progress(1.0f))
                send(DownloadStatus.Completed)
                return@channelFlow
            }

            val url = entity.audioUrl
            val fileName = "$soundId.mp3"
            val soundsDir = storageProvider.getAppDataDir().div("sounds")
            if (!fileSystem.exists(soundsDir)) fileSystem.createDirectory(soundsDir)

            val targetFile = soundsDir.div(fileName)
            val tempFile = soundsDir.div("$fileName.tmp")

            // Ktor ile indirme
            client.prepareGet(url) {
                // Ktor callback'i içinden güvenle 'send' çağırabiliriz
                onDownload { bytesSentTotal, contentLength ->
                    if (contentLength != null && contentLength > 0) {
                        val progress = (bytesSentTotal.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                        try {
                            send(DownloadStatus.Progress(progress))
                        } catch (e: Exception) {
                            // Channel kapandıysa hata vermesin
                        }
                    }
                }
            }.execute { httpResponse ->
                val channel: ByteReadChannel = httpResponse.bodyAsChannel()

                fileSystem.sink(tempFile).buffer().use { sink ->
                    val buffer = ByteArray(8192)
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead <= 0) break
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }

            fileSystem.atomicMove(tempFile, targetFile)
            queries.updateLocalPath(targetFile.toString(), soundId)

            send(DownloadStatus.Completed)

        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val tempPath = storageProvider.getAppDataDir().div("sounds").div("$soundId.mp3.tmp")
                if (fileSystem.exists(tempPath)) fileSystem.delete(tempPath)
            } catch (ignore: Exception) {}

            send(DownloadStatus.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)


    // 2. ESKİ FONKSİYON (Geriye Dönük Uyumluluk)
    // Flow'u kullanıp sadece sonuca bakar.
    override suspend fun downloadSound(soundId: String): Boolean {
        var isSuccess = false
        downloadSoundFlow(soundId).collect { status ->
            if (status is DownloadStatus.Completed) isSuccess = true
        }
        return isSuccess
    }
}