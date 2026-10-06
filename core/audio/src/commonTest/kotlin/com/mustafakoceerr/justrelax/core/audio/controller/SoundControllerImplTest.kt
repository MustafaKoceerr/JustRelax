package com.mustafakoceerr.justrelax.core.audio.controller

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Toggling sounds on and off within a generated mix keeps each sound's volume. */
class SoundControllerImplTest {

    private val mixer = FakeAudioMixer()
    private val controller = SoundControllerImpl(
        audioMixer = mixer,
        playSoundUseCase = PlaySoundUseCase(
            FakeSoundRepository(listOf(testSound("rain", localPath = "/sounds/rain.mp3"))),
            mixer,
        ),
    )

    private fun volumeOf(id: String) = mixer.state.value.activeSounds.single { it.id == id }.initialVolume

    @Test
    fun unknownSound_startsAtTheDefaultVolume() = runTest {
        controller.toggleSound("rain")

        assertEquals(AudioDefaults.BASE_VOLUME, volumeOf("rain"))
    }

    @Test
    fun soundFromAGeneratedMix_comesBackAtItsMixVolume() = runTest {
        controller.setVolumes(mapOf("rain" to 0.8f))

        controller.toggleSound("rain")

        assertEquals(0.8f, volumeOf("rain"))
    }

    @Test
    fun togglingOffAndOn_keepsTheLastVolume() = runTest {
        controller.toggleSound("rain")
        controller.changeVolume("rain", 0.3f)

        controller.toggleSound("rain") // off
        assertTrue(mixer.state.value.activeSounds.isEmpty())
        controller.toggleSound("rain") // on again

        assertEquals(0.3f, volumeOf("rain"))
    }
}
