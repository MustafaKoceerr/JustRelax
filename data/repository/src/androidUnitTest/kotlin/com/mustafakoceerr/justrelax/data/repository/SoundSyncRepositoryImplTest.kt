package com.mustafakoceerr.justrelax.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.testing.TestDispatcherProvider
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SoundSyncRepositoryImplTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        JustRelaxDatabase.Schema.create(it)
    }
    private val database = createDatabase(driver)
    private val mapper = DatabaseSoundMapper()
    private val remote = FakeSoundRemoteDataSource(listOf(testSound("rain"), testSound("fire")))

    @AfterTest
    fun tearDown() = driver.close()

    private fun TestScope.dispatchers() = TestDispatcherProvider(StandardTestDispatcher(testScheduler))

    private fun TestScope.syncRepository() = SoundSyncRepositoryImpl(remote, database, mapper, dispatchers())

    private fun TestScope.savedMixRepository() =
        SavedMixRepositoryImpl(database.savedMixQueries, database.soundQueries, mapper, dispatchers())

    private fun localSounds() = database.soundQueries.selectAllSounds().executeAsList().associateBy { it.id }

    @Test
    fun firstSync_insertsAllRemoteSounds() = runTest {
        val result = syncRepository().syncWithServer()

        assertIs<Resource.Success<Unit>>(result)
        assertEquals(setOf("rain", "fire"), localSounds().keys)
    }

    @Test
    fun resyncWithChangedMetadata_updatesSound_butKeepsLocalPathAndSavedMixes() = runTest {
        syncRepository().syncWithServer()
        database.soundQueries.updateLocalPath(localPath = "/sounds/rain.mp3", id = "rain")
        savedMixRepository().saveMix("Night", mapOf("rain" to 0.4f, "fire" to 0.7f))

        remote.sounds = listOf(testSound("rain").copy(sizeBytes = 42), testSound("fire"))
        syncRepository().syncWithServer()

        val rain = localSounds().getValue("rain")
        assertEquals(42L, rain.sizeBytes)
        assertEquals("/sounds/rain.mp3", rain.localPath)
        val mix = savedMixRepository().getSavedMixes().first().single()
        assertEquals(setOf("rain", "fire"), mix.sounds.keys.map { it.id }.toSet())
    }

    @Test
    fun soundRemovedOnServer_isDeleted_andDroppedFromSavedMixes() = runTest {
        syncRepository().syncWithServer()
        savedMixRepository().saveMix("Night", mapOf("rain" to 0.4f, "fire" to 0.7f))

        remote.sounds = listOf(testSound("fire"))
        syncRepository().syncWithServer()

        assertEquals(setOf("fire"), localSounds().keys)
        val relations = database.savedMixQueries.selectAllRelations().executeAsList().map { it.sound_id }
        assertEquals(listOf("fire"), relations)
    }

    @Test
    fun remoteFailure_returnsError_andKeepsLocalData() = runTest {
        syncRepository().syncWithServer()
        remote.error = IllegalStateException("offline")

        val result = syncRepository().syncWithServer()

        assertIs<Resource.Error>(result)
        assertEquals(setOf("rain", "fire"), localSounds().keys)
    }
}
