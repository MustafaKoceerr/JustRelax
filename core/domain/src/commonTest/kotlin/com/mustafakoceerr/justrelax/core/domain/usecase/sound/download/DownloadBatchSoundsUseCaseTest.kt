package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.testing.fake.FakeFileDownloadRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLocalStorageRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DownloadBatchSoundsUseCaseTest {

    private val sounds = (1..5).map { testSound("s$it") }
    private val storage = FakeLocalStorageRepository()
    private val downloader = FakeFileDownloadRepository(storage)
    private val soundRepository = FakeSoundRepository(sounds)

    private val useCase = DownloadBatchSoundsUseCase(
        DownloadSingleSoundUseCase(storage, downloader, soundRepository)
    )

    @Test
    fun emptyList_completesImmediately() = runTest {
        useCase(emptyList()).test {
            assertEquals(DownloadStatus.Completed, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allSucceed_progressIsMonotonic_andEndsWithCompleted() = runTest {
        val statuses = useCase(sounds).toList()

        assertEquals(DownloadStatus.Completed, statuses.last())
        val progress = statuses.filterIsInstance<DownloadStatus.Progress>().map { it.percentage }
        assertEquals(progress.sorted(), progress)
        assertTrue(soundRepository.getSounds().first().all { it.isDownloaded })
    }

    @Test
    fun oneFailure_doesNotStopOthers_andEndsWithError() = runTest {
        downloader.onDownload = { url -> !url.contains("s3") }

        val statuses = useCase(sounds).toList()

        assertIs<DownloadStatus.Error>(statuses.last())
        val downloaded = soundRepository.getSounds().first().filter { it.isDownloaded }.map { it.id }
        assertEquals(listOf("s1", "s2", "s4", "s5"), downloaded)
    }

    @Test
    fun thrownException_isTreatedAsFailure_andOthersContinue() = runTest {
        downloader.onDownload = { url -> if (url.contains("s2")) error("boom") else true }

        val statuses = useCase(sounds).toList()

        assertIs<DownloadStatus.Error>(statuses.last())
        assertEquals(4, soundRepository.getSounds().first().count { it.isDownloaded })
    }

    @Test
    fun atMostThreeDownloadsRunConcurrently() = runTest {
        var running = 0
        var maxRunning = 0
        downloader.onDownload = {
            running++
            maxRunning = maxOf(maxRunning, running)
            delay(100)
            running--
            true
        }

        useCase(sounds).toList()

        assertEquals(3, maxRunning)
    }
}
