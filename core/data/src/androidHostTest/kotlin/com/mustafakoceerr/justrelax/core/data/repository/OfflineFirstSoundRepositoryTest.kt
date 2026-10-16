package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.download.FakeFileDownloader
import com.mustafakoceerr.justrelax.core.data.download.FakeSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.download.SoundDownloader
import com.mustafakoceerr.justrelax.core.data.mapper.DatabaseSoundMapper
import com.mustafakoceerr.justrelax.core.data.sync.FakeSoundCatalogSync
import com.mustafakoceerr.justrelax.core.data.sync.FakeSyncTimestampStore
import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.testing.TestDispatcherProvider
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class OfflineFirstSoundRepositoryTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        JustRelaxDatabase.Schema.create(it)
    }
    private val database = createDatabase(driver)

    private val now = Instant.fromEpochMilliseconds(1_000_000_000_000)
    private val clock = object : Clock {
        override fun now(): Instant = now
    }
    private val catalogSync = FakeSoundCatalogSync()
    private val syncTimestamps = FakeSyncTimestampStore()
    private val storage = FakeSoundFileStorage()
    private val fileDownloader = FakeFileDownloader(storage)

    @AfterTest
    fun tearDown() = driver.close()

    private fun TestScope.repository() = OfflineFirstSoundRepository(
        soundQueries = database.soundQueries,
        soundMapper = DatabaseSoundMapper(),
        catalogSync = catalogSync,
        syncTimestamps = syncTimestamps,
        downloader = SoundDownloader(storage, fileDownloader),
        clock = clock,
        dispatchers = TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
    )

    private fun insert(vararg sounds: Sound) = sounds.forEach { sound ->
        database.soundQueries.insertSound(
            id = sound.id,
            names = sound.names,
            categoryId = sound.categoryId,
            iconUrl = sound.iconUrl,
            remoteUrl = sound.remoteUrl,
            localPath = sound.localPath,
            isInitial = sound.isInitial,
            sizeBytes = sound.sizeBytes,
        )
    }

    // --- sync

    @Test
    fun syncedRecently_doesNotHitServer() = runTest {
        syncTimestamps.lastSync.value = (now - 23.hours).toEpochMilliseconds()

        val result = repository().syncIfStale()

        assertIs<Resource.Success<Unit>>(result)
        assertEquals(0, catalogSync.syncCount)
    }

    @Test
    fun syncOlderThan24Hours_syncsAndStoresTimestamp() = runTest {
        syncTimestamps.lastSync.value = (now - 25.hours).toEpochMilliseconds()

        val result = repository().syncIfStale()

        assertIs<Resource.Success<Unit>>(result)
        assertEquals(1, catalogSync.syncCount)
        assertEquals(now.toEpochMilliseconds(), syncTimestamps.lastSync.value)
    }

    @Test
    fun failedSync_keepsOldTimestamp_andReturnsError() = runTest {
        catalogSync.result = Resource.Error(AppError.Network.NoInternet())
        val lastSync = (now - 48.hours).toEpochMilliseconds()
        syncTimestamps.lastSync.value = lastSync

        val result = repository().syncIfStale()

        assertIs<Resource.Error>(result)
        assertEquals(lastSync, syncTimestamps.lastSync.value)
    }

    // --- downloads

    @Test
    fun download_storesTheLocalPath() = runTest {
        insert(testSound("rain"))
        val repository = repository()

        val success = repository.download("rain")

        assertTrue(success)
        assertEquals("/sounds/rain.mp3", repository.getSound("rain").first()?.localPath)
    }

    @Test
    fun starterPack_downloadsOnlyInitialSoundsThatAreMissing() = runTest {
        insert(
            testSound("rain", isInitial = true),
            testSound("fire", isInitial = true, localPath = "/sounds/fire.mp3"),
            testSound("city", isInitial = false),
        )
        val repository = repository()

        val statuses = repository.downloadStarterPack().toList()

        assertEquals(DownloadStatus.Completed, statuses.last())
        assertEquals(listOf("https://cdn.test/rain.mp3"), fileDownloader.requestedUrls)
        assertEquals("/sounds/rain.mp3", repository.getSound("rain").first()?.localPath)
    }

    @Test
    fun starterPack_syncFailure_emitsError_andDownloadsNothing() = runTest {
        insert(testSound("rain", isInitial = true))
        catalogSync.result = Resource.Error(AppError.Network.NoInternet())

        val statuses = repository().downloadStarterPack().toList()

        assertIs<DownloadStatus.Error>(statuses.single())
        assertTrue(fileDownloader.requestedUrls.isEmpty())
    }

    @Test
    fun downloadAll_downloadsEveryMissingSound() = runTest {
        insert(testSound("rain"), testSound("fire", localPath = "/sounds/fire.mp3"), testSound("city"))

        val statuses = repository().downloadAll().toList()

        assertEquals(DownloadStatus.Completed, statuses.last())
        assertEquals(
            setOf("https://cdn.test/rain.mp3", "https://cdn.test/city.mp3"),
            fileDownloader.requestedUrls.toSet(),
        )
    }
}
