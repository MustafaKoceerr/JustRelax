package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MixerViewModelTest {

    private val downloaded = (1..4).map { testSound("s$it", localPath = "/sounds/s$it.mp3") } +
        (1..4).map { testSound("e$it", localPath = "/sounds/e$it.ogg", kind = SoundKind.EVENT) }
    private val repository = FakeSoundRepository(downloaded + testSound("not_downloaded"))
    private val scene = InMemorySceneRepository(repository)
    private val playback = FakePlaybackController()

    private fun TestScope.viewModel() = MixerViewModel(
        soundRepository = repository,
        sceneRepository = scene,
        languageController = FakeLanguageController(),
        sceneGenerator = RandomSceneGenerator(Random(42)),
        playbackController = playback,
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
        assertEquals(state.mixedSounds.map { it.id }.toSet(), state.sceneCards.playingIds)
        assertTrue(state.mixedSounds.none { it.id == "not_downloaded" })
    }

    @Test
    fun soundCount_isKeptBetweenTwoAndFive() = runMainTest {
        val viewModel = viewModel()

        viewModel.selectSoundCount(1)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.selectedSoundCount)


        viewModel.selectSoundCount(12)
        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.value.selectedSoundCount)
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
        val mixVolume = viewModel.uiState.value.sceneCards.volumes.getValue(sound.id)

        viewModel.toggleSound(sound.id)
        advanceUntilIdle()
        assertFalse(sound.id in viewModel.uiState.value.sceneCards.playingIds)

        viewModel.toggleSound(sound.id)
        advanceUntilIdle()
        assertEquals(mixVolume, viewModel.uiState.value.sceneCards.volumes[sound.id])
    }

    @Test
    fun changingVolume_isReflected() = runMainTest {
        val viewModel = viewModel()
        viewModel.generateMix()
        advanceUntilIdle()
        val sound = viewModel.uiState.value.mixedSounds.first()

        viewModel.changeVolume(sound.id, 0.25f)
        advanceUntilIdle()

        assertEquals(0.25f, viewModel.uiState.value.sceneCards.volumes[sound.id])
    }

    @Test
    fun aGeneratedScene_mixesBedsAndEvents_andEventFrequenciesCanBeChanged() = runMainTest {
        val viewModel = viewModel()
        viewModel.selectSoundCount(4)
        viewModel.generateMix()
        advanceUntilIdle()
        val event = viewModel.uiState.value.mixedSounds.first { it.kind == SoundKind.EVENT }

        viewModel.changeFrequency(event.id, EventFrequency.RARE)
        advanceUntilIdle()

        assertEquals(2, scene.scene.value.beds.size)
        assertEquals(EventFrequency.RARE, viewModel.uiState.value.sceneCards.frequencies[event.id])
    }
}
