package com.mustafakoceerr.justrelax.core.domain.usecase.sound

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadBatchSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.testing.fake.FakeDataSourceStateRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeFileDownloadRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLocalStorageRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundSyncRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DownloadInitialSoundsUseCaseTest {

    private val storage = FakeLocalStorageRepository()
    private val downloader = FakeFileDownloadRepository(storage)
    private val syncRepository = FakeSoundSyncRepository()
    private val soundRepository = FakeSoundRepository(
        listOf(
            testSound("rain", isInitial = true),
            testSound("fire", isInitial = true, localPath = "/sounds/fire.mp3"),
            testSound("city", isInitial = false),
        )
    )

    private val useCase = DownloadInitialSoundsUseCase(
        syncSoundsUseCase = SyncSoundsUseCase(syncRepository, FakeDataSourceStateRepository()),
        soundRepository = soundRepository,
        downloadBatchSoundsUseCase = DownloadBatchSoundsUseCase(
            DownloadSingleSoundUseCase(storage, downloader, soundRepository)
        ),
    )

    @Test
    fun downloadsOnlyInitialSoundsThatAreMissing() = runTest {
        val statuses = useCase().toList()

        assertEquals(DownloadStatus.Completed, statuses.last())
        assertEquals(listOf("https://cdn.test/rain.mp3"), downloader.requestedUrls)
    }

    @Test
    fun syncFailure_emitsError_andDownloadsNothing() = runTest {
        syncRepository.result = Resource.Error(AppError.Network.NoInternet())

        val statuses = useCase().toList()

        assertIs<DownloadStatus.Error>(statuses.single())
        assertTrue(downloader.requestedUrls.isEmpty())
    }
}
