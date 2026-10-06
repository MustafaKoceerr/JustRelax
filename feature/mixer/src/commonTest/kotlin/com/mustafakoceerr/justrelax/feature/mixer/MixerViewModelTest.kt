package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.audio.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MixerViewModelTest {

    private val downloaded = (1..8).map { testSound("s$it", localPath = "/sounds/s$it.mp3") }
    private val repository = FakeSoundRepository(downloaded + testSound("not_downloaded"))
    private val mixer = FakeAudioMixer()

    private fun TestScope.viewModel() = MixerViewModel(
        generateRandomMixUseCase = GenerateRandomMixUseCase(repository, FakeLanguageController()),
        setMixUseCase = SetMixUseCase(mixer),
        soundController = SoundControllerImpl(mixer, PlaySoundUseCase(repository, mixer)),
    ).also { observe(it.uiState) }

    @Test
    fun generatingAMix_playsTheSelectedNumberOfDownloadedSounds() = runMainTest {
        val viewModel = viewModel()
        viewModel.selectSoundCount(3)

        viewModel.generateMix()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertEquals(3, state.mixedSounds.size)
        assertEquals(state.mixedSounds.map { it.id }.toSet(), state.playingSoundIds)
        assertTrue(state.mixedSounds.none { it.id == "not_downloaded" })
    }

    @Test
    fun soundCount_isKeptBetweenTwoAndSeven() = runMainTest {
        val viewModel = viewModel()

        viewModel.selectSoundCount(1)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.selectedSoundCount)


        viewModel.selectSoundCount(12)
        advanceUntilIdle()
        assertEquals(7, viewModel.uiState.value.selectedSoundCount)
    }

    @Test
    fun noDownloadedSounds_explainsWhy() = runMainTest {
        repository.sounds.value = listOf(testSound("not_downloaded"))
        val viewModel = viewModel()

        viewModel.generateMix()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.userMessage)
        assertTrue(viewModel.uiState.value.mixedSounds.isEmpty())
    }

    @Test
    fun togglingASoundInTheMix_stopsItAndKeepsItsVolumeForLater() = runMainTest {
        val viewModel = viewModel()
        viewModel.generateMix()
        advanceUntilIdle()
        val sound = viewModel.uiState.value.mixedSounds.first()
        val mixVolume = viewModel.uiState.value.soundVolumes.getValue(sound.id)

        viewModel.toggleSound(sound.id)
        advanceUntilIdle()
        assertFalse(sound.id in viewModel.uiState.value.playingSoundIds)

        viewModel.toggleSound(sound.id)
        advanceUntilIdle()
        assertEquals(mixVolume, viewModel.uiState.value.soundVolumes[sound.id])
    }

    @Test
    fun changingVolume_isReflected() = runMainTest {
        val viewModel = viewModel()
        viewModel.generateMix()
        advanceUntilIdle()
        val sound = viewModel.uiState.value.mixedSounds.first()

        viewModel.changeVolume(sound.id, 0.25f)
        advanceUntilIdle()

        assertEquals(0.25f, viewModel.uiState.value.soundVolumes[sound.id])
    }
}
