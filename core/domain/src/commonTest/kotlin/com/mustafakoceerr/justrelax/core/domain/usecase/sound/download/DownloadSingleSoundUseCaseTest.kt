package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.testing.fake.FakeFileDownloadRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLocalStorageRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DownloadSingleSoundUseCaseTest {

    private val storage = FakeLocalStorageRepository(soundsDirectory = "/sounds")
    private val downloader = FakeFileDownloadRepository(storage)
    private val soundRepository = FakeSoundRepository(listOf(testSound("rain")))

    private val useCase = DownloadSingleSoundUseCase(storage, downloader, soundRepository)

    private suspend fun localPathOf(id: String) = soundRepository.getSound(id).first()?.localPath

    @Test
    fun successfulDownload_movesTempToFinalPath_andUpdatesLocalPath() = runTest {
        val result = useCase("rain", "https://cdn.test/rain.mp3")

        assertTrue(result)
        assertEquals(setOf("/sounds/rain.mp3"), storage.files)
        assertEquals("/sounds/rain.mp3", localPathOf("rain"))
    }

    @Test
    fun alreadyDownloadedFile_isNotDownloadedAgain() = runTest {
        storage.files += "/sounds/rain.mp3"

        val result = useCase("rain", "https://cdn.test/rain.mp3")

        assertTrue(result)
        assertTrue(downloader.requestedUrls.isEmpty())
        assertEquals("/sounds/rain.mp3", localPathOf("rain"))
    }

    @Test
    fun failedDownload_returnsFalse_andLeavesNoFiles() = runTest {
        downloader.onDownload = { false }

        val result = useCase("rain", "https://cdn.test/rain.mp3")

        assertFalse(result)
        assertTrue(storage.files.isEmpty())
        assertNull(localPathOf("rain"))
    }

    @Test
    fun staleTempFile_isDeletedBeforeDownload() = runTest {
        storage.files += "/sounds/rain.mp3.tmp"
        downloader.onDownload = { false }

        useCase("rain", "https://cdn.test/rain.mp3")

        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun urlWithoutExtension_fallsBackToM4a() = runTest {
        useCase("rain", "https://cdn.test/sounds/rain")

        assertEquals(setOf("/sounds/rain.m4a"), storage.files)
    }

    @Test
    fun urlWithQueryString_ignoresQueryWhenResolvingExtension() = runTest {
        useCase("rain", "https://cdn.test/rain.ogg?v=2")

        assertEquals(setOf("/sounds/rain.ogg"), storage.files)
    }

    @Test
    fun cancellationDuringDownload_isPropagated() = runTest {
        downloader.onDownload = { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> {
            useCase("rain", "https://cdn.test/rain.mp3")
        }
    }
}
