package com.mustafakoceerr.justrelax.data.repository


import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.system.FileDownloadRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

internal class FileDownloadRepositoryImpl(
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem,
    private val dispatchers: DispatcherProvider
) : FileDownloadRepository {

    /**
     * Returns true only if the server answered 2xx and the whole body was written.
     * On any failure the (possibly partial) destination file is removed.
     */
    override suspend fun downloadFile(url: String, destinationPath: String): Boolean = withContext(dispatchers.io) {
        val destination = destinationPath.toPath()
        try {
            httpClient.prepareGet(url).execute { httpResponse ->
                if (!httpResponse.status.isSuccess()) return@execute false

                val expectedLength = httpResponse.contentLength()
                val channel = httpResponse.bodyAsChannel()
                var writtenBytes = 0L
                fileSystem.sink(destination).buffer().use { sink ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead < 0) break
                        sink.write(buffer, 0, bytesRead)
                        writtenBytes += bytesRead
                    }
                }
                // A stream that was cut off early must not count as a complete download.
                channel.closedCause?.let { throw it }
                expectedLength == null || writtenBytes == expectedLength
            }
        } catch (e: CancellationException) {
            deleteQuietly(destination)
            throw e
        } catch (e: Exception) {
            false
        }.also { success -> if (!success) deleteQuietly(destination) }
    }

    private fun deleteQuietly(path: Path) {
        runCatching { fileSystem.delete(path) }
    }

    private companion object {
        const val BUFFER_SIZE = 8 * 1024
    }
}
