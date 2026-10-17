package com.mustafakoceerr.justrelax.data.repository

import com.mustafakoceerr.justrelax.core.testing.TestDispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writer
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.IOException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileDownloadRepositoryImplTest {

    private val fileSystem = FakeFileSystem().apply { createDirectories("/sounds".toPath()) }
    private val destination = "/sounds/rain.mp3.tmp"
    private val audioBytes = ByteArray(20_000) { (it % 251).toByte() }

    @AfterTest
    fun tearDown() = fileSystem.checkNoOpenFiles()

    private fun TestScope.repository(handler: MockRequestHandler) = FileDownloadRepositoryImpl(
        httpClient = HttpClient(MockEngine(handler)),
        fileSystem = fileSystem,
        dispatchers = TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
    )

    @Test
    fun successfulResponse_writesBodyToDestination() = runTest {
        val repository = repository { respond(audioBytes) }

        val result = repository.downloadFile("https://cdn.test/rain.mp3", destination)

        assertTrue(result)
        assertContentEquals(audioBytes, fileSystem.read(destination.toPath()) { readByteArray() })
    }

    @Test
    fun notFoundResponse_returnsFalse_andCreatesNoFile() = runTest {
        val repository = repository { respond("<html>Not Found</html>", HttpStatusCode.NotFound) }

        val result = repository.downloadFile("https://cdn.test/rain.mp3", destination)

        assertFalse(result)
        assertFalse(fileSystem.exists(destination.toPath()))
    }

    @Test
    fun serverError_returnsFalse_andCreatesNoFile() = runTest {
        val repository = repository { respond("oops", HttpStatusCode.InternalServerError) }

        assertFalse(repository.downloadFile("https://cdn.test/rain.mp3", destination))
        assertFalse(fileSystem.exists(destination.toPath()))
    }

    @Test
    fun connectionFailure_returnsFalse() = runTest {
        val repository = repository { throw IOException("no network") }

        assertFalse(repository.downloadFile("https://cdn.test/rain.mp3", destination))
        assertFalse(fileSystem.exists(destination.toPath()))
    }

    @Test
    fun connectionClosedBeforeContentLength_returnsFalse_andDeletesPartialFile() = runTest {
        val repository = repository {
            val body: ByteReadChannel = backgroundScope.writer {
                channel.writeFully(audioBytes, 0, audioBytes.size / 2)
            }.channel
            respond(
                content = body,
                headers = headersOf(HttpHeaders.ContentLength, audioBytes.size.toString()),
            )
        }

        val result = repository.downloadFile("https://cdn.test/rain.mp3", destination)

        assertFalse(result)
        assertFalse(fileSystem.exists(destination.toPath()))
    }
}
