package com.mustafakoceerr.justrelax.feature.player

import com.mustafakoceerr.justrelax.feature.player.usecase.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedMixRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.player.generated.resources.Res
import justrelax.feature.player.generated.resources.err_mix_save_name_exists
import justrelax.feature.player.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerViewModelTest {

    private val sounds = FakeSoundRepository(
        listOf(testSound("rain", localPath = "/sounds/rain.mp3"), testSound("fire", localPath = "/sounds/fire.mp3"))
    )
    private val scene = InMemorySceneRepository(sounds)
    private val playback = FakePlaybackController()
    private val savedMixes = FakeSavedMixRepository()

    private suspend fun TestScope.viewModel(): PlayerViewModel {
        scene.toggle("rain")
        playback.play()
        return PlayerViewModel(
            soundRepository = sounds,
            sceneRepository = scene,
            playbackController = playback,
            saveCurrentMixUseCase = SaveCurrentMixUseCase(savedMixes, scene),
        ).also { observe(it.uiState) }
    }

    private fun PlayerViewModel.messageRes() = (uiState.value.userMessage as UiText.Resource).resId

    @Test
    fun stateShowsTheSoundsCurrentlyPlaying() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("rain"), viewModel.uiState.value.activeSounds.map { it.id })
        assertTrue(viewModel.uiState.value.isPlaying)
        assertTrue(viewModel.uiState.value.isVisible)
    }

    @Test
    fun togglePlayPause_pausesAndResumesTheMix() = runMainTest {
        val viewModel = viewModel()

        viewModel.togglePlayPause()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isPlaying)

        viewModel.togglePlayPause()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun savingMix_closesDialog_andConfirms() = runMainTest {
        val viewModel = viewModel()
        viewModel.openSaveDialog()

        viewModel.saveMix("Night")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaveDialogVisible)
        assertEquals(Res.string.msg_mix_saved_success, viewModel.messageRes())
        assertEquals(1, savedMixes.mixes.value.size)
    }

    @Test
    fun duplicateName_keepsDialogOpen_andExplains() = runMainTest {
        val viewModel = viewModel()
        viewModel.saveMix("Night")
        advanceUntilIdle()
        viewModel.onMessageShown()

        viewModel.openSaveDialog()
        viewModel.saveMix("night")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaveDialogVisible)
        assertEquals(Res.string.err_mix_save_name_exists, viewModel.messageRes())
        assertEquals(1, savedMixes.mixes.value.size)
    }

    @Test
    fun shownMessage_isCleared() = runMainTest {
        val viewModel = viewModel()
        viewModel.saveMix("Night")
        advanceUntilIdle()

        viewModel.onMessageShown()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun stopAll_clearsTheScene_andHidesThePlayer() = runMainTest {
        val viewModel = viewModel()

        viewModel.stopAll()
        advanceUntilIdle()

        assertTrue(scene.scene.value.layers.isEmpty())
        assertFalse(viewModel.uiState.value.isVisible)
    }
}
