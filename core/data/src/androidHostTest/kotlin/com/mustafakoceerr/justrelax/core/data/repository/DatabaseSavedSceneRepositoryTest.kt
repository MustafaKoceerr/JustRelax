package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.inMemoryDriver
import com.mustafakoceerr.justrelax.core.data.mapper.insert
import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeClock
import com.mustafakoceerr.justrelax.core.testing.testDispatchers
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class DatabaseSavedSceneRepositoryTest {

    private val driver = inMemoryDriver()
    private val database = createDatabase(driver).apply {
        soundQueries.insert(testSound("rain"))
        soundQueries.insert(testSound("birds", kind = SoundKind.EVENT))
    }

    private val clock = FakeClock(Instant.fromEpochMilliseconds(1_000))

    private val night = Scene(listOf(Layer.Bed("rain", 0.3f), Layer.Event("birds", 0.8f, EventFrequency.RARE)))

    @AfterTest
    fun tearDown() = driver.close()

    private fun TestScope.repository() = DatabaseSavedSceneRepository(
        queries = database.savedSceneQueries,
        clock = clock,
        dispatchers = testDispatchers(),
    )

    private fun errorOf(result: Resource<Unit>) = assertIs<Resource.Error>(result).error

    @Test
    fun aSavedScene_comesBackWithItsLayersInOrder_volumesAndFrequencies() = runTest {
        val repository = repository()

        assertIs<Resource.Success<Unit>>(repository.save("Night", night))

        val saved = repository.getSavedScenes().first().single()
        assertEquals("Night", saved.name)
        assertEquals(clock.now(), saved.createdAt)
        assertEquals(night, saved.scene)
    }

    @Test
    fun theNewestSceneComesFirst() = runTest {
        val repository = repository()
        repository.save("Old", night)
        clock.current = Instant.fromEpochMilliseconds(2_000)
        repository.save("New", night)

        assertEquals(listOf("New", "Old"), repository.getSavedScenes().first().map { it.name })
    }

    @Test
    fun theNameIsTrimmed() = runTest {
        val repository = repository()

        repository.save("  Night  ", night)

        assertEquals("Night", repository.getSavedScenes().first().single().name)
    }

    @Test
    fun aBlankName_isRejected() = runTest {
        assertIs<AppError.SaveMix.EmptyName>(errorOf(repository().save("   ", night)))
    }

    @Test
    fun anEmptyScene_isRejected() = runTest {
        assertIs<AppError.SaveMix.NoSoundsPlaying>(errorOf(repository().save("Night", Scene.EMPTY)))
    }

    @Test
    fun aNameInUse_isRejected_ignoringCase() = runTest {
        val repository = repository()
        repository.save("Night", night)

        assertIs<AppError.SaveMix.NameAlreadyExists>(errorOf(repository.save("night ", night)))
        assertEquals(1, repository.getSavedScenes().first().size)
    }

    @Test
    fun aDatabaseFailure_isASaveFailure_andSavesNothing() = runTest {
        val repository = repository()

        val result = repository.save("Night", Scene(listOf(Layer.Bed("not_in_the_library", 0.5f))))

        assertIs<AppError.Database.SaveFailed>(errorOf(result))
        assertTrue(repository.getSavedScenes().first().isEmpty())
    }

    @Test
    fun delete_removesTheScene() = runTest {
        val repository = repository()
        repository.save("Night", night)

        repository.delete(repository.getSavedScenes().first().single().id)

        assertTrue(repository.getSavedScenes().first().isEmpty())
    }
}
