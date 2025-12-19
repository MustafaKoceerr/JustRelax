package com.mustafakoceerr.justrelax.data.repository

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.FileDownloadRepository
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

internal class FileDownloadRepositoryImpl(
    // DEĞİŞİKLİK: HttpClient'ı Koin'den 'get()' ile alacağız,
    // doğrudan KtorClient'a bağımlı olmayalım.
    private val httpClient: HttpClient,
    private val dispatchers: DispatcherProvider
) : FileDownloadRepository {

    override fun downloadFile(url: String, destinationPath: String): Flow<DownloadStatus> = callbackFlow {
        withContext(dispatchers.io) {
            try {
                trySend(DownloadStatus.Queued)

                httpClient.get(url) {
                    onDownload { bytesSentTotal, contentLength ->
                        // DÜZELTME: 'contentLength' null olabilir, bunu kontrol et.
                        val safeContentLength = contentLength ?: 0L

                        val progress = if (safeContentLength > 0) {
                            // Artık 'safeContentLength' null olmadığı için güvenle işlem yapabiliriz.
                            (bytesSentTotal.toFloat() / safeContentLength.toFloat())
                        } else {
                            // Eğer sunucu dosya boyutunu vermediyse, ilerlemeyi
                            // belirsiz olarak kabul edip 0f veya özel bir durum dönebiliriz.
                            // Şimdilik 0f en güvenlisi.
                            0f
                        }
                        trySend(DownloadStatus.Progress(progress))
                    }
                }.body<ByteArray>().let { fileBytes ->
                    FileSystem.SYSTEM.write(destinationPath.toPath()) {
                        write(fileBytes)
                    }
                    trySend(DownloadStatus.Completed)
                }
            } catch (e: Exception) {
                trySend(DownloadStatus.Error(e.message ?: "Unknown download error"))
            } finally {
                close()
            }
        }
    }
}