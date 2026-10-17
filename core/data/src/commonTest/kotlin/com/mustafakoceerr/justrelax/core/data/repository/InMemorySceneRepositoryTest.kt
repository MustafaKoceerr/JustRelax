package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InMemorySceneRepositoryTest {

    private val downloaded = (1..12).map { testSound("s$it", localPath = "/sounds/s$it.mp3") }
    private val sounds = FakeSoundRepository(downloaded + testSound("cloud"))
    private val repository = InMemorySceneRepository(sounds)

    private fun ids() = repository.scene.value.layers.map { it.soundId }

    @Test
    fun toggle_addsADownloadedSoundAtTheDefaultVolume_andRemovesItAgain() = runTest {
        assertIs<Resource.Success<Unit>>(repository.toggle("s1"))
        assertEquals(Layer.Bed("s1", AudioDefaults.BASE_VOLUME), repository.scene.value.layer("s1"))

        repository.toggle("s1")
        assertEquals(emptyList(), ids())
    }

    @Test
    fun toggle_soundThatIsNotDownloaded_isAnError() = runTest {
        val result = assertIs<Resource.Error>(repository.toggle("cloud"))

        assertIs<AppError.Storage.FileNotFound>(result.error)
        assertTrue(ids().isEmpty())
    }

    @Test
    fun toggle_unknownSound_isAnError() = runTest {
        assertIs<Resource.Error>(repository.toggle("nope"))
    }

    @Test
    fun toggle_beyondTheLimit_isAnError_andKeepsTheScene() = runTest {
        (1..Scene.MAX_LAYERS).forEach { repository.toggle("s$it") }

        val result = assertIs<Resource.Error>(repository.toggle("s11"))

        assertIs<AppError.Player.LimitExceeded>(result.error)
        assertEquals(Scene.MAX_LAYERS, ids().size)
    }

    @Test
    fun aSoundComesBackAtItsLastVolume() = runTest {
        repository.toggle("s1")
        repository.setVolume("s1", 0.8f)
        repository.toggle("s1")

        repository.toggle("s1")

        assertEquals(0.8f, repository.scene.value.layer("s1")?.volume)
    }

    @Test
    fun replaceWith_playsOnlyDownloadedSounds_upToTheLimit() = runTest {
        repository.toggle("s1")

        repository.replaceWith(mapOf("cloud" to 0.5f) + (2..12).associate { "s$it" to 0.3f })

        assertEquals((2..11).map { "s$it" }, ids())
        assertEquals(0.3f, repository.scene.value.layer("s2")?.volume)
    }

    @Test
    fun replaceWith_remembersVolumes_andClampsThem() = runTest {
        repository.replaceWith(mapOf("s1" to 1.4f, "s2" to 0.2f))
        assertEquals(1f, repository.scene.value.layer("s1")?.volume)

        repository.toggle("s2")
        repository.toggle("s2")

        assertEquals(0.2f, repository.scene.value.layer("s2")?.volume)
    }

    @Test
    fun clear_emptiesTheScene() = runTest {
        repository.toggle("s1")

        repository.clear()

        assertEquals(Scene.EMPTY, repository.scene.value)
    }
}
