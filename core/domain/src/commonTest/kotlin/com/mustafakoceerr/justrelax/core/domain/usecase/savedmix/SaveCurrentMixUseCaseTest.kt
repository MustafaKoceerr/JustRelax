package com.mustafakoceerr.justrelax.core.domain.usecase.savedmix

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedMixRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SaveCurrentMixUseCaseTest {

    private val mixer = FakeAudioMixer()
    private val repository = FakeSavedMixRepository()
    private val useCase = SaveCurrentMixUseCase(repository, GetGlobalMixerStateUseCase(mixer))

    private fun playing(vararg volumes: Pair<String, Float>) = mixer.setState(
        GlobalMixerState(
            isPlaying = true,
            activeSounds = volumes.map { (id, volume) -> SoundConfig(id, "/sounds/$id.mp3", volume) },
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
