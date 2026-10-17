package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlaySoundUseCaseTest {

    private val mixer = FakeAudioMixer()
    private val repository = FakeSoundRepository(
        listOf(testSound("rain", localPath = "/sounds/rain.mp3"), testSound("fire"))
    )
    private val useCase = PlaySoundUseCase(repository, mixer)

    @Test
    fun downloadedSound_isPlayedFromLocalPath() = runTest {
        val result = useCase("rain", initialVolume = 0.4f)

        assertIs<Resource.Success<Unit>>(result)
        val config = mixer.playedConfigs.single()
        assertEquals("/sounds/rain.mp3", config.url)
        assertEquals(0.4f, config.initialVolume)
    }

    @Test
    fun volumeIsClampedToValidRange() = runTest {
        useCase("rain", initialVolume = 3f)

        assertEquals(1f, mixer.playedConfigs.single().initialVolume)
    }

    @Test
    fun notDownloadedSound_returnsFileNotFound_andDoesNotPlay() = runTest {
        val result = useCase("fire")

        assertIs<AppError.Storage.FileNotFound>(assertIs<Resource.Error>(result).error)
        assertTrue(mixer.playedConfigs.isEmpty())
    }

    @Test
    fun unknownSound_returnsFileNotFound() = runTest {
        assertIs<Resource.Error>(useCase("unknown"))
    }
}
