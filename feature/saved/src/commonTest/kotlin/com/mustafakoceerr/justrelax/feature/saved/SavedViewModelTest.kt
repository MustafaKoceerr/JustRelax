package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.model.SavedMix
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedMixRepository
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

class SavedViewModelTest {

    private val rain = testSound("rain", localPath = "/sounds/rain.mp3")
    private val fire = testSound("fire", localPath = "/sounds/fire.mp3")
    private val nightMix = SavedMix(id = 1, name = "Night", createdAt = "2026-10-06", sounds = mapOf(rain to 0.4f, fire to 0.7f))

    private val repository = FakeSavedMixRepository().apply { mixes.value = listOf(nightMix) }
    private val scene = InMemorySceneRepository(FakeSoundRepository(listOf(rain, fire)))
    private val playback = FakePlaybackController()

    private fun TestScope.viewModel() = SavedViewModel(
        savedMixRepository = repository,
        sceneRepository = scene,
        playbackController = playback,
    ).also { observe(it.uiState) }

    @Test
    fun savedMixesAreListed() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Night"), state.mixes.map { it.title })
        assertEquals(2, state.mixes.single().icons.size)
    }

    @Test
    fun playingAMix_loadsItIntoTheMixer() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.playMix(mixId = 1)
        advanceUntilIdle()

        val active = scene.scene.value.layers
        assertEquals(setOf("rain", "fire"), active.map { it.soundId }.toSet())
        assertEquals(0.4f, active.first { it.soundId == "rain" }.volume)
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
        assertEquals(mapOf("rain" to 0.4f, "fire" to 0.7f), repository.savedVolumes["Night"])
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
}
