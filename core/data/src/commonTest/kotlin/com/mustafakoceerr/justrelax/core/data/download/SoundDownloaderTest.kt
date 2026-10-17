package com.mustafakoceerr.justrelax.core.data.download

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SoundDownloaderTest {

    private val storage = FakeSoundFileStorage(soundsDirectory = "/sounds")
    private val fileDownloader = FakeFileDownloader(storage)
    private val downloader = SoundDownloader(storage, fileDownloader)

    private val sounds = (1..5).map { testSound("s$it") }
    private val downloaded = mutableMapOf<String, String>()
    private fun downloadAll() = downloader.downloadAll(sounds) { sound, path -> downloaded[sound.id] = path }

    // --- single sound

    @Test
    fun successfulDownload_movesTempToFinalPath() = runTest {
        val path = downloader.download("rain", "https://cdn.test/rain.mp3")

        assertEquals("/sounds/rain.mp3", path)
        assertEquals(setOf("/sounds/rain.mp3"), storage.files)
    }

    @Test
    fun alreadyDownloadedFile_isNotDownloadedAgain() = runTest {
        storage.files += "/sounds/rain.mp3"

        val path = downloader.download("rain", "https://cdn.test/rain.mp3")

        assertEquals("/sounds/rain.mp3", path)
        assertTrue(fileDownloader.requestedUrls.isEmpty())
    }

    @Test
    fun failedDownload_returnsNull_andLeavesNoFiles() = runTest {
        fileDownloader.onDownload = { false }

        val path = downloader.download("rain", "https://cdn.test/rain.mp3")

        assertNull(path)
        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun staleTempFile_isDeletedBeforeDownload() = runTest {
        storage.files += "/sounds/rain.mp3.tmp"
        fileDownloader.onDownload = { false }

        downloader.download("rain", "https://cdn.test/rain.mp3")

        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun urlWithoutExtension_fallsBackToM4a() = runTest {
        downloader.download("rain", "https://cdn.test/sounds/rain")

        assertEquals(setOf("/sounds/rain.m4a"), storage.files)
    }

    @Test
    fun urlWithQueryString_ignoresQueryWhenResolvingExtension() = runTest {
        downloader.download("rain", "https://cdn.test/rain.ogg?v=2")

        assertEquals(setOf("/sounds/rain.ogg"), storage.files)
    }

    @Test
    fun cancellationDuringDownload_isPropagated() = runTest {
        fileDownloader.onDownload = { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> {
            downloader.download("rain", "https://cdn.test/rain.mp3")
        }
    }

    // --- batch

    @Test
    fun emptyList_completesImmediately() = runTest {
        downloader.downloadAll(emptyList()) { _, _ -> }.test {
            assertEquals(DownloadStatus.Completed, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allSucceed_progressIsMonotonic_andEndsWithCompleted() = runTest {
        val statuses = downloadAll().toList()

        assertEquals(DownloadStatus.Completed, statuses.last())
        val progress = statuses.filterIsInstance<DownloadStatus.Progress>().map { it.percentage }
        assertEquals(progress.sorted(), progress)
        assertEquals(sounds.map { it.id }.toSet(), downloaded.keys)
    }

    @Test
    fun oneFailure_doesNotStopOthers_andEndsWithError() = runTest {
        fileDownloader.onDownload = { url -> !url.contains("s3") }

        val statuses = downloadAll().toList()

        assertIs<DownloadStatus.Error>(statuses.last())
        assertEquals(listOf("s1", "s2", "s4", "s5"), downloaded.keys.sorted())
    }

    @Test
    fun thrownException_isTreatedAsFailure_andOthersContinue() = runTest {
        fileDownloader.onDownload = { url -> if (url.contains("s2")) error("boom") else true }

        val statuses = downloadAll().toList()

        assertIs<DownloadStatus.Error>(statuses.last())
        assertEquals(4, downloaded.size)
    }

    @Test
    fun atMostThreeDownloadsRunConcurrently() = runTest {
        var running = 0
        var maxRunning = 0
        fileDownloader.onDownload = {
            running++
            maxRunning = maxOf(maxRunning, running)
            delay(100)
            running--
            true
        }

        downloadAll().toList()

        assertEquals(3, maxRunning)
    }
}
