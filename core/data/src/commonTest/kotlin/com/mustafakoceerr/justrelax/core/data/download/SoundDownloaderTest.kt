package com.mustafakoceerr.justrelax.core.data.download

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.testing.testClip
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

    private val storage = FakeSoundFileStorage(catalogDirectory = "/sounds")
    private val fileDownloader = FakeFileDownloader(storage)
    private val downloader = SoundDownloader(storage, fileDownloader)

    private val rain = testSound("rain")
    private val birds = testSound(
        "birds",
        clips = (0..2).map { testClip("https://cdn.test/birds_$it.ogg") },
    )

    private val sounds = (1..5).map { testSound("s$it") }
    private val downloaded = mutableMapOf<String, List<String>>()
    private fun downloadAll() = downloader.downloadAll(sounds) { sound, paths -> downloaded[sound.id] = paths }

    // --- one sound

    @Test
    fun everyClip_isDownloadedToItsOwnFile_inOrder() = runTest {
        val paths = downloader.download(birds)

        assertEquals(listOf("/sounds/birds_0.ogg", "/sounds/birds_1.ogg", "/sounds/birds_2.ogg"), paths)
        assertEquals(paths!!.toSet(), storage.files)
    }

    @Test
    fun aClipThatIsAlreadyDownloaded_isNotDownloadedAgain() = runTest {
        val partlyDownloaded = birds.copy(clips = birds.clips.mapIndexed { index, clip ->
            if (index == 0) clip.copy(localPath = "/sounds/birds_0.ogg") else clip
        })
        storage.files += "/sounds/birds_0.ogg"

        val paths = downloader.download(partlyDownloaded)

        assertEquals("/sounds/birds_0.ogg", paths?.first())
        assertEquals(listOf("https://cdn.test/birds_1.ogg", "https://cdn.test/birds_2.ogg"), fileDownloader.requestedUrls)
    }

    @Test
    fun aFileLeftFromAnEarlierAttempt_isReused() = runTest {
        storage.files += "/sounds/rain_0.mp3"

        val paths = downloader.download(rain)

        assertEquals(listOf("/sounds/rain_0.mp3"), paths)
        assertTrue(fileDownloader.requestedUrls.isEmpty())
    }

    @Test
    fun oneFailedClip_failsTheSound_butKeepsTheOthersForTheNextTry() = runTest {
        fileDownloader.onDownload = { url -> !url.endsWith("birds_1.ogg") }

        assertNull(downloader.download(birds))
        assertEquals(setOf("/sounds/birds_0.ogg"), storage.files)

        fileDownloader.onDownload = { true }
        fileDownloader.requestedUrls.clear()
        downloader.download(birds)
        assertEquals(listOf("https://cdn.test/birds_1.ogg", "https://cdn.test/birds_2.ogg"), fileDownloader.requestedUrls)
    }

    @Test
    fun aClipWithoutARemoteUrl_cannotBeDownloaded() = runTest {
        assertNull(downloader.download(testSound("mine", clips = listOf(testClip(remoteUrl = null)))))
    }

    @Test
    fun staleTempFile_isDeletedBeforeDownload() = runTest {
        storage.files += "/sounds/rain_0.mp3.tmp"
        fileDownloader.onDownload = { false }

        downloader.download(rain)

        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun urlWithoutExtension_fallsBackToM4a() = runTest {
        downloader.download(testSound("rain", clips = listOf(testClip("https://cdn.test/sounds/rain"))))

        assertEquals(setOf("/sounds/rain_0.m4a"), storage.files)
    }

    @Test
    fun urlWithQueryString_ignoresQueryWhenResolvingExtension() = runTest {
        downloader.download(testSound("rain", clips = listOf(testClip("https://cdn.test/rain.ogg?v=2"))))

        assertEquals(setOf("/sounds/rain_0.ogg"), storage.files)
    }

    @Test
    fun cancellationDuringDownload_isPropagated() = runTest {
        fileDownloader.onDownload = { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> { downloader.download(rain) }
    }

    // --- many sounds

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
    fun atMostThreeSoundsDownloadConcurrently() = runTest {
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
