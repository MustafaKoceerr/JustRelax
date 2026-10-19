package com.mustafakoceerr.justrelax.feature.scene

import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSleepTimer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedSceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SceneViewModelTest {

    private val sounds = FakeSoundRepository(
        listOf(
            testSound("rain", localPath = "/sounds/rain.mp3", category = SoundCategory.RAIN)
                .copy(names = mapOf("en" to "Rain", "tr" to "Yağmur")),
            testSound("fire", localPath = "/sounds/fire.mp3"),
            testSound("owl", localPath = "/sounds/owl.ogg", kind = SoundKind.EVENT, defaultFrequency = EventFrequency.RARE),
        )
    )
    private val language = FakeLanguageController()
    private val scene = InMemorySceneRepository(sounds)
    private val playback = FakePlaybackController()
    private val savedScenes = FakeSavedSceneRepository()
    private val analytics = TestAnalyticsHelper()
    private val sleepTimer = FakeSleepTimer()

    private suspend fun TestScope.viewModel(): SceneViewModel {
        scene.toggle("rain")
        playback.play()
        return SceneViewModel(
            soundRepository = sounds,
            sceneRepository = scene,
            playbackController = playback,
            savedSceneRepository = savedScenes,
            languageController = language,
            analytics = analytics,
            sleepTimer = sleepTimer,
        ).also {
            observe(it.uiState)
            observe(it.volumes)
            observe(it.timerRemaining)
        }
    }

    @Test
    fun stateShowsTheSoundsCurrentlyPlaying() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("rain"), viewModel.uiState.value.layers.map { it.soundId })
        assertTrue(viewModel.uiState.value.isPlaying)
        assertTrue(viewModel.uiState.value.isVisible)
    }

    @Test
    fun theMiniPlayerNamesItsSounds_inTheAppLanguage() = runMainTest {
        language.currentLanguage.value = AppLanguage.TURKISH
        val viewModel = viewModel()
        scene.toggle("fire")
        viewModel.miniPlayer.test {
            var state = awaitItem()
            while (state.names.size < 2) state = awaitItem()
            assertEquals(listOf("Yağmur", "fire"), state.names)
        }
    }

    /** Volume drags change the scene many times a second; the mini player must not redraw for them. */
    @Test
    fun theMiniPlayerIgnoresVolumeChanges() = runMainTest {
        val viewModel = viewModel()
        viewModel.miniPlayer.test {
            var state = awaitItem()
            if (!state.isVisible) state = awaitItem()
            assertTrue(state.isVisible)
            assertTrue(state.isPlaying)

            viewModel.changeVolume("rain", 0.3f)
            advanceUntilIdle()
            expectNoEvents()

            viewModel.togglePlayPause()
            assertFalse(awaitItem().isPlaying)
        }
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
        assertEquals(SceneMessage.Saved("Night"), viewModel.uiState.value.userMessage)
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
        assertEquals(SceneMessage.NameTaken, viewModel.uiState.value.userMessage)
    }

    @Test
    fun theSaveDialog_canStartWithASuggestedName() = runMainTest {
        val viewModel = viewModel()

        viewModel.openSaveDialog("Rainy cafe")
        advanceUntilIdle()
        assertEquals("Rainy cafe", viewModel.uiState.value.saveDialogName)

        viewModel.dismissSaveDialog()
        viewModel.openSaveDialog()
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.saveDialogName)
    }

    @Test
    fun theTimersRemainingTime_showsWhileItRunsOrIsPaused() = runMainTest {
        val viewModel = viewModel()
        assertNull(viewModel.timerRemaining.value)

        sleepTimer.start(90)
        advanceUntilIdle()
        assertEquals(90L, viewModel.timerRemaining.value)

        sleepTimer.pause()
        advanceUntilIdle()
        assertEquals(90L, viewModel.timerRemaining.value)

        sleepTimer.cancel()
        advanceUntilIdle()
        assertNull(viewModel.timerRemaining.value)
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

    // --- The scene panel

    @Test
    fun layers_showTheirNameInTheAppLanguage_icon_andVolume() = runMainTest {
        language.currentLanguage.value = AppLanguage.TURKISH
        val viewModel = viewModel()
        advanceUntilIdle()

        val rain = viewModel.uiState.value.layers.single()
        assertEquals("Yağmur", rain.name)
        assertEquals(SoundCategory.RAIN.defaultIcon, rain.icon)
        assertEquals(0.5f, viewModel.volumes.value["rain"])
        assertNull(rain.frequency)
    }

    @Test
    fun anEventLayer_showsItsFrequency_andItCanBeChanged() = runMainTest {
        val viewModel = viewModel()
        scene.toggle("owl")
        advanceUntilIdle()
        assertEquals(EventFrequency.RARE, viewModel.uiState.value.layers.last().frequency)

        viewModel.changeFrequency("owl", EventFrequency.OFTEN)
        advanceUntilIdle()

        assertEquals(EventFrequency.OFTEN, viewModel.uiState.value.layers.last().frequency)
    }

    @Test
    fun changeVolume_changesTheLayer() = runMainTest {
        val viewModel = viewModel()

        viewModel.changeVolume("rain", 0.9f)
        advanceUntilIdle()

        assertEquals(0.9f, viewModel.volumes.value["rain"])
    }

    @Test
    fun changeVolume_leavesThePanelStateAlone() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.changeVolume("rain", 0.9f)
        advanceUntilIdle()

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun remove_takesTheSoundOutOfTheScene() = runMainTest {
        val viewModel = viewModel()
        scene.toggle("fire")

        viewModel.remove("rain")
        advanceUntilIdle()

        assertEquals(listOf("fire"), viewModel.uiState.value.layers.map { it.soundId })
    }

    @Test
    fun aSavedMixIsCountedWithItsSize_butNotItsName() = runMainTest {
        val viewModel = viewModel()

        viewModel.saveMix("Night")
        advanceUntilIdle()
        savedScenes.saveResult = Resource.Error(AppError.SaveMix.NameAlreadyExists())
        viewModel.saveMix("Night")
        advanceUntilIdle()

        assertEquals(listOf(AnalyticsEvent(EventTypes.MIX_SAVED, mapOf("layer_count" to "1"))), analytics.events)
    }

    @Test
    fun startTimer_startsTheSleepTimerForTheScene() = runMainTest {
        val viewModel = viewModel()

        viewModel.startTimer(30 * 60L)
        advanceUntilIdle()

        assertEquals(30 * 60L, viewModel.timerRemaining.value)
        assertTrue(analytics.events.any { it.type == EventTypes.TIMER_STARTED })
    }

    @Test
    fun startTimer_doesNothingWithoutAScene() = runMainTest {
        val viewModel = viewModel()
        viewModel.stopAll()

        viewModel.startTimer(15 * 60L)
        advanceUntilIdle()

        assertNull(viewModel.timerRemaining.value)
    }

    @Test
    fun cancelTimer_stopsTheCountdown() = runMainTest {
        val viewModel = viewModel()
        viewModel.startTimer(60 * 60L)

        viewModel.cancelTimer()
        advanceUntilIdle()

        assertNull(viewModel.timerRemaining.value)
        assertEquals(1, sleepTimer.cancelCount)
    }
}
