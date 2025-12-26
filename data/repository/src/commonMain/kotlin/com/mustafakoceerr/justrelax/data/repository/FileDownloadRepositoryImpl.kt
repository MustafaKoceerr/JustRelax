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
            // 1. prepareGet: Bağlantıyı kurar ama veriyi hemen indirmez.
            httpClient.prepareGet(url).execute { httpResponse ->
                val channel = httpResponse.bodyAsChannel()

                // 2. Okio ile dosyaya yazma akışı (Sink) açıyoruz.
                // .buffer() yazma işlemini hızlandırır.
                FileSystem.SYSTEM.sink(destinationPath.toPath()).buffer().use { sink ->

                    // 3. Sadece 8 KB'lık küçük bir tampon bellek kullanıyoruz.
                    val buffer = ByteArray(8 * 1024)

                    while (!channel.isClosedForRead) {
                        // Ağdan oku
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead < 0) break

                        // Diske yaz
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
            // Hata almadan buraya geldiyse işlem tamamdır.
            true
        } catch (e: Exception) {
            // Hata durumunda false dönüyoruz, üst katman (UseCase) bunu yönetecek.
            e.printStackTrace()
            false
        }
    }
}