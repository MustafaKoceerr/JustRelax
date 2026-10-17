package com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.testing.fake.FakeDataSourceStateRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundSyncRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class SyncSoundsIfNecessaryUseCaseTest {

    private val now = Instant.fromEpochMilliseconds(1_000_000_000_000)
    private val clock = object : Clock {
        override fun now(): Instant = now
    }
    private val syncRepository = FakeSoundSyncRepository()

    private fun useCase(lastSync: Instant) = SyncSoundsIfNecessaryUseCase(
        dataSourceStateRepository = stateRepository(lastSync),
        soundSyncRepository = syncRepository,
        clock = clock,
    )

    private var stateRepository: FakeDataSourceStateRepository? = null
    private fun stateRepository(lastSync: Instant) =
        FakeDataSourceStateRepository(lastSync.toEpochMilliseconds()).also { stateRepository = it }

    @Test
    fun syncedRecently_doesNotHitServer() = runTest {
        val result = useCase(lastSync = now - 23.hours)()

        assertIs<Resource.Success<Unit>>(result)
        assertEquals(0, syncRepository.syncCount)
    }

    @Test
    fun syncOlderThan24Hours_syncsAndStoresTimestamp() = runTest {
        val result = useCase(lastSync = now - 25.hours)()

        assertIs<Resource.Success<Unit>>(result)
        assertEquals(1, syncRepository.syncCount)
        assertEquals(now.toEpochMilliseconds(), stateRepository!!.lastSync.value)
    }

    @Test
    fun failedSync_keepsOldTimestamp_andReturnsError() = runTest {
        syncRepository.result = Resource.Error(AppError.Network.NoInternet())
        val lastSync = now - 48.hours

        val result = useCase(lastSync)()

        assertIs<Resource.Error>(result)
        assertEquals(lastSync.toEpochMilliseconds(), stateRepository!!.lastSync.value)
    }
}
