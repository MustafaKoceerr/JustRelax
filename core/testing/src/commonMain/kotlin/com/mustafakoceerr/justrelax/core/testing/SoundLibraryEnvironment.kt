package com.mustafakoceerr.justrelax.core.testing

import com.mustafakoceerr.justrelax.core.domain.usecase.sound.GetSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadBatchSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.testing.fake.FakeDataSourceStateRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeFileDownloadRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLocalStorageRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundSyncRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The real sound sync/download use cases wired to fakes, for ViewModel tests.
 * Tweak behaviour through the exposed fakes (e.g. `downloader.onDownload`, `syncRepository.result`).
 */
@OptIn(ExperimentalTime::class)
class SoundLibraryEnvironment(sounds: List<Sound> = emptyList()) {
    val soundRepository = FakeSoundRepository(sounds)
    val storage = FakeLocalStorageRepository()
    val downloader = FakeFileDownloadRepository(storage)
    val syncRepository = FakeSoundSyncRepository()
    val dataSourceState = FakeDataSourceStateRepository()

    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_000_000_000_000)
    }

    val getSounds = GetSoundsUseCase(soundRepository)
    val syncSounds = SyncSoundsUseCase(syncRepository, dataSourceState)
    val syncSoundsIfNecessary = SyncSoundsIfNecessaryUseCase(dataSourceState, syncRepository, clock)

    private val downloadBatch = DownloadBatchSoundsUseCase(
        DownloadSingleSoundUseCase(storage, downloader, soundRepository)
    )
    val downloadInitial = DownloadInitialSoundsUseCase(syncSounds, soundRepository, downloadBatch)
    val downloadAll = DownloadAllSoundsUseCase(syncSounds, soundRepository, downloadBatch)
}
