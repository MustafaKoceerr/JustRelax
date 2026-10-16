package com.mustafakoceerr.justrelax.feature.scene

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedSceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.scene.generated.resources.Res
import justrelax.feature.scene.generated.resources.err_mix_save_name_exists
import justrelax.feature.scene.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SceneViewModelTest {

    private val sounds = FakeSoundRepository(
        listOf(testSound("rain", localPath = "/sounds/rain.mp3"), testSound("fire", localPath = "/sounds/fire.mp3"))
    )
    private val scene = InMemorySceneRepository(sounds)
    private val playback = FakePlaybackController()
    private val savedScenes = FakeSavedSceneRepository()

    private suspend fun TestScope.viewModel(): SceneViewModel {
        scene.toggle("rain")
        playback.play()
        return SceneViewModel(
            soundRepository = sounds,
            sceneRepository = scene,
            playbackController = playback,
            savedSceneRepository = savedScenes,
        ).also { observe(it.uiState) }
    }

    private fun SceneViewModel.messageRes() = (uiState.value.userMessage as UiText.Resource).resId

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
    fun saving_storesTheCurrentScene_closesTheDialog_andConfirms() = runMainTest {
        val viewModel = viewModel()
        viewModel.openSaveDialog()

        viewModel.saveMix("Night")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaveDialogVisible)
        assertEquals(Res.string.msg_mix_saved_success, viewModel.messageRes())
        val saved = savedScenes.scenes.value.single()
        assertEquals("Night", saved.name)
        assertEquals(scene.scene.value, saved.scene)
    }

    @Test
    fun aRejectedName_keepsTheDialogOpen_andExplains() = runMainTest {
        val viewModel = viewModel()
        savedScenes.saveResult = Resource.Error(AppError.SaveMix.NameAlreadyExists())
        viewModel.openSaveDialog()

        viewModel.saveMix("night")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaveDialogVisible)
        assertEquals(Res.string.err_mix_save_name_exists, viewModel.messageRes())
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
