package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundscapeEngine
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PlaySoundUseCaseTest {

    private val mixer = FakeSoundscapeEngine()
    private val repository = FakeSoundRepository(
        listOf(testSound("rain", localPath = "/sounds/rain.mp3"), testSound("fire"))
    )
    private val useCase = PlaySoundUseCase(repository, mixer)

    @Test
    fun downloadedSound_isPlayedFromLocalPath() = runTest {
        val result = useCase("rain", volume = 0.4f)

        assertIs<Resource.Success<Unit>>(result)
        val layer = assertIs<Layer.Bed>(mixer.addedLayers.single())
        assertEquals("/sounds/rain.mp3", layer.clip)
        assertEquals(0.4f, layer.volume)
    }

    @Test
    fun volumeIsClampedToValidRange() = runTest {
        useCase("rain", volume = 3f)

        assertEquals(1f, mixer.addedLayers.single().volume)
    }

    @Test
    fun notDownloadedSound_returnsFileNotFound_andDoesNotPlay() = runTest {
        val result = useCase("fire")

        assertIs<AppError.Storage.FileNotFound>(assertIs<Resource.Error>(result).error)
        assertTrue(mixer.addedLayers.isEmpty())
    }

    @Test
    fun unknownSound_returnsFileNotFound() = runTest {
        assertIs<Resource.Error>(useCase("unknown"))
    }
}
