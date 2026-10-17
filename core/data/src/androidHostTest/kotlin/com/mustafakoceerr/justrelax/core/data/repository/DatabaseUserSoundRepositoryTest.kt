package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mustafakoceerr.justrelax.core.data.download.FakeSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.mapper.insert
import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.TestDispatcherProvider
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioTrimmer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DatabaseUserSoundRepositoryTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        JustRelaxDatabase.Schema.create(it)
    }
    private val database = createDatabase(driver)
    private val storage = FakeSoundFileStorage()
    private val trimmer = FakeAudioTrimmer().apply { onTrim = { storage.files += it } }
    private val soundRepository = FakeSoundRepository()
    private val sceneRepository = InMemorySceneRepository(soundRepository)

    private val draft = UserSoundDraft(
        name = "  Kettle  ",
        category = SoundCategory.CITY,
        icon = SoundIcon.CAFE,
        kind = SoundKind.EVENT,
        sourcePath = "/cache/import/kettle",
        range = TrimRange(1_000, 4_000),
    )

    @AfterTest
    fun tearDown() = driver.close()

    private fun TestScope.repository() = DatabaseUserSoundRepository(
        soundQueries = database.soundQueries,
        storage = storage,
        trimmer = trimmer,
        sceneRepository = sceneRepository,
        dispatchers = TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
        newId = { "user-1" },
    )

    @Test
    fun create_trimsIntoTheUserDirectoryAndStoresAnOwnUserSound() = runTest {
        val repository = repository()

        val sound = repository.create(draft)

        assertEquals(FakeAudioTrimmer.Call("/cache/import/kettle", TrimRange(1_000, 4_000), "/user/user-1.m4a"), trimmer.calls.single())
        assertEquals("user-1", sound.id)
        assertEquals(mapOf("en" to "Kettle"), sound.names)
        assertEquals(SoundSource.USER, sound.source)
        assertEquals(License.OWN, sound.license)
        assertEquals(SoundKind.EVENT, sound.kind)
        assertEquals(SoundIcon.CAFE, sound.icon)
        assertEquals(listOf(SoundClip(remoteUrl = null, localPath = "/user/user-1.m4a", durationMs = 3_000, sizeBytes = 30_000)), sound.clips)
        assertEquals(listOf(sound), repository.getUserSounds().first())
    }

    @Test
    fun create_whenTrimmingFails_leavesNeitherASoundNorAFile() = runTest {
        trimmer.error = MediaException("codec")
        val repository = repository()

        assertFailsWith<MediaException> { repository.create(draft) }

        assertTrue(storage.files.isEmpty())
        assertEquals(emptyList(), repository.getUserSounds().first())
    }

    @Test
    fun getUserSounds_leavesOutCatalogSounds() = runTest {
        database.soundQueries.insert(testSound("rain", localPath = "/sounds/rain.mp3"))
        val repository = repository()

        repository.create(draft)

        assertEquals(listOf("user-1"), repository.getUserSounds().first().map { it.id })
    }

    @Test
    fun delete_removesTheFileTheSavedSceneLayersAndTheSoundFromThePlayingScene() = runTest {
        database.soundQueries.insert(testSound("rain", localPath = "/sounds/rain.mp3"))
        val repository = repository()
        val sound = repository.create(draft)
        database.savedSceneQueries.insertScene("Evening", 1L)
        database.savedSceneQueries.insertLayer(1L, "rain", 0L, 0.5, null)
        database.savedSceneQueries.insertLayer(1L, "user-1", 1L, 0.5, null)
        soundRepository.sounds.value = listOf(sound)
        sceneRepository.toggle("user-1")

        repository.delete("user-1")

        assertTrue(storage.files.isEmpty())
        assertEquals(emptyList(), repository.getUserSounds().first())
        assertEquals(listOf("rain"), database.savedSceneQueries.selectAllLayers().executeAsList().map { it.sound_id })
        assertNull(sceneRepository.scene.value.layer("user-1"))
    }
}
