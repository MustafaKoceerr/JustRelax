package com.mustafakoceerr.justrelax.data.repository


import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.system.FileDownloadRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

internal class FileDownloadRepositoryImpl(
    private val httpClient: HttpClient,
    private val dispatchers: DispatcherProvider
) : FileDownloadRepository {

    override suspend fun downloadFile(url: String, destinationPath: String): Boolean = withContext(dispatchers.io) {
        try {
            httpClient.prepareGet(url).execute { httpResponse ->
                val channel = httpResponse.bodyAsChannel()
                FileSystem.SYSTEM.sink(destinationPath.toPath()).buffer().use { sink ->
                    val buffer = ByteArray(8 * 1024)
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead < 0) break
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}