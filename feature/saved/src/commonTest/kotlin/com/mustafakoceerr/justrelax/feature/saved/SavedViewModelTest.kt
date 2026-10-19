package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.model.SavedScene
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedSceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class SavedViewModelTest {

    private val rain = testSound("rain", localPath = "/sounds/rain.mp3", category = SoundCategory.RAIN)
    private val fire = testSound("fire", localPath = "/sounds/fire.mp3", kind = SoundKind.EVENT)
    private val night = SavedScene(
        id = 1,
        name = "Night",
        createdAt = Instant.fromEpochMilliseconds(0),
        scene = Scene(listOf(Layer.Bed("rain", 0.4f), Layer.Event("fire", 0.7f, EventFrequency.RARE))),
    )

    private val repository = FakeSavedSceneRepository().apply { scenes.value = listOf(night) }
    private val sounds = FakeSoundRepository(listOf(rain, fire))
    private val scene = InMemorySceneRepository(sounds)
    private val playback = FakePlaybackController()

    private fun TestScope.viewModel() = SavedViewModel(
        savedSceneRepository = repository,
        soundRepository = sounds,
        sceneRepository = scene,
        languageController = FakeLanguageController(),
        playbackController = playback,
    ).also { observe(it.uiState) }

    @Test
    fun savedScenesAreListed_withTheirSoundsIcons() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Night"), state.mixes.map { it.title })
        assertEquals(listOf(rain.icon, fire.icon), state.mixes.single().icons)
    }

    @Test
    fun eachMix_namesItsSounds() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("rain", "fire"), viewModel.uiState.value.mixes.single().soundNames)
    }

    @Test
    fun theMixThatPlays_isMarked_untilItIsPaused() = runMainTest {
        val viewModel = viewModel()
        viewModel.playMix(mixId = 1)
        advanceUntilIdle()
        assertEquals(1L, viewModel.uiState.value.playingMixId)

        playback.pause()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.playingMixId)
    }

    @Test
    fun aMixStaysMarked_whenItsVolumesAreChanged_butNotWhenItsSoundsAre() = runMainTest {
        val viewModel = viewModel()
        viewModel.playMix(mixId = 1)
        advanceUntilIdle()

        scene.setVolume("rain", 0.9f)
        advanceUntilIdle()
        assertEquals(1L, viewModel.uiState.value.playingMixId)

        scene.toggle("fire")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.playingMixId)
    }

    @Test
    fun togglePlay_playsAMix_andPausesItWhenItIsPlaying() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.togglePlay(mixId = 1)
        advanceUntilIdle()
        assertEquals(night.scene, scene.scene.value)
        assertTrue(playback.isPlaying.value)

        viewModel.togglePlay(mixId = 1)
        advanceUntilIdle()
        assertFalse(playback.isPlaying.value)
        assertEquals(1, playback.playCount)
    }

    @Test
    fun twoMixesWithTheSameSounds_areToldApartByWhichOneWasPlayed() = runMainTest {
        val louder = night.copy(id = 2, name = "Loud night", scene = Scene(listOf(Layer.Bed("rain", 0.9f), Layer.Event("fire", 0.9f, EventFrequency.OFTEN))))
        repository.scenes.value = listOf(night, louder)
        val viewModel = viewModel()

        viewModel.togglePlay(mixId = 2)
        advanceUntilIdle()
        assertEquals(2L, viewModel.uiState.value.playingMixId)

        viewModel.togglePlay(mixId = 1)
        advanceUntilIdle()
        assertEquals(1L, viewModel.uiState.value.playingMixId)
        assertEquals(night.scene, scene.scene.value)
        assertTrue(playback.isPlaying.value)
    }

    @Test
    fun aSceneMatchingTwoMixes_thatNoneOfThemStarted_marksNeither() = runMainTest {
        repository.scenes.value = listOf(night, night.copy(id = 2, name = "Copy"))
        val viewModel = viewModel()

        scene.replaceWith(night.scene)
        playback.play()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.playingMixId)
    }

    @Test
    fun playingAScene_makesItTheCurrentScene_andPlays() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.playMix(mixId = 1)
        advanceUntilIdle()

        assertEquals(night.scene, scene.scene.value)
        assertEquals(1, playback.playCount)
    }

    @Test
    fun deletingAMix_removesIt_andOffersUndo() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.deleteMix(mixId = 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.mixes.isEmpty())
        assertNotNull(state.message?.actionLabel)
    }

    @Test
    fun undo_restoresTheDeletedMix() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.deleteMix(mixId = 1)
        advanceUntilIdle()

        viewModel.undoDelete()
        advanceUntilIdle()

        assertEquals(listOf("Night"), viewModel.uiState.value.mixes.map { it.title })
        assertEquals(night.scene, repository.scenes.value.single().scene)
    }

    @Test
    fun shownMessage_isCleared() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.deleteMix(mixId = 1)
        advanceUntilIdle()

        viewModel.onMessageShown()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.message)
    }

    // --- Detail (shown beside the list on wide windows)

    @Test
    fun selectingAScene_showsItsSounds() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectMix(mixId = 1)
        advanceUntilIdle()

        val detail = assertNotNull(viewModel.uiState.value.selected)
        assertEquals("Night", detail.title)
        assertEquals(listOf("rain", "fire"), detail.sounds.map { it.name })
        assertEquals(listOf(0.4f, 0.7f), detail.sounds.map { it.volume })
        assertEquals(listOf(null, EventFrequency.RARE), detail.sounds.map { it.frequency })
    }

    @Test
    fun deletingTheSelectedScene_clearsTheSelection() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.selectMix(mixId = 1)

        viewModel.deleteMix(mixId = 1)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selected)
    }
}
