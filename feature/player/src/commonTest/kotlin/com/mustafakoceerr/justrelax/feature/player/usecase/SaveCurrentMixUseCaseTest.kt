package com.mustafakoceerr.justrelax.feature.player.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeState
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedMixRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundscapeEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SaveCurrentMixUseCaseTest {

    private val mixer = FakeSoundscapeEngine()
    private val repository = FakeSavedMixRepository()
    private val useCase = SaveCurrentMixUseCase(repository, mixer)

    private fun playing(vararg volumes: Pair<String, Float>) = mixer.setState(
        SoundscapeState(
            isPlaying = true,
            scene = Scene(volumes.map { (id, volume) -> Layer.Bed(id, clip = "/sounds/$id.ogg", volume = volume) }),
        )
    )

    private fun errorOf(result: Resource<Unit>) = assertIs<Resource.Error>(result).error

    @Test
    fun blankName_isRejected() = runTest {
        playing("rain" to 0.5f)

        assertIs<AppError.SaveMix.EmptyName>(errorOf(useCase("   ")))
    }

    @Test
    fun nothingPlaying_isRejected() = runTest {
        assertIs<AppError.SaveMix.NoSoundsPlaying>(errorOf(useCase("Night")))
    }

    @Test
    fun savesActiveSoundsWithTheirCurrentVolumes() = runTest {
        playing("rain" to 0.3f, "fire" to 0.8f)

        val result = useCase("Night")

        assertIs<Resource.Success<Unit>>(result)
        assertEquals(mapOf("rain" to 0.3f, "fire" to 0.8f), repository.savedVolumes["Night"])
    }

    @Test
    fun nameIsTrimmedBeforeSaving() = runTest {
        playing("rain" to 0.5f)

        useCase("  Night  ")

        assertTrue("Night" in repository.savedVolumes)
    }

    @Test
    fun duplicateName_isRejected_caseInsensitive() = runTest {
        playing("rain" to 0.5f)
        useCase("Night")

        assertIs<AppError.SaveMix.NameAlreadyExists>(errorOf(useCase("night ")))
        assertEquals(1, repository.mixes.value.size)
    }

    @Test
    fun repositoryFailure_isMappedToSaveFailed() = runTest {
        playing("rain" to 0.5f)
        repository.saveError = IllegalStateException("disk")

        assertIs<AppError.Database.SaveFailed>(errorOf(useCase("Night")))
    }
}
