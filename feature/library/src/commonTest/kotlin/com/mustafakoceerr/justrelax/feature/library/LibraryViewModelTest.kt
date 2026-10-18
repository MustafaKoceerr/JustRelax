package com.mustafakoceerr.justrelax.feature.library

import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.StarterPackInstaller
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.library.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.library.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.library.generated.resources.download_failed
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibraryViewModelTest {

    private val repository = FakeSoundRepository(
        listOf(
            testSound("rain", localPath = "/sounds/rain.mp3", category = SoundCategory.RAIN),
            testSound("storm", category = SoundCategory.RAIN),
            testSound("forest", localPath = "/sounds/forest.mp3", category = SoundCategory.NATURE),
            testSound(
                "owl",
                localPath = "/sounds/owl.ogg",
                category = SoundCategory.NATURE,
                kind = SoundKind.EVENT,
                defaultFrequency = EventFrequency.RARE,
            ),
        )
    )
    private val scene = InMemorySceneRepository(repository)
    private val playback = FakePlaybackController()
    private val setup = FakeAppSetupRepository(installed = true)
    private val analytics = TestAnalyticsHelper()

    private fun TestScope.viewModel() = LibraryViewModel(
        getLocalizedCategorizedSoundsUseCase = GetLocalizedCategorizedSoundsUseCase(repository, FakeLanguageController()),
        sceneRepository = scene,
        playbackController = playback,
        soundRepository = repository,
        starterPackInstaller = StarterPackInstaller(repository, setup, scope = this),
        analytics = analytics,
    ).also { observe(it.uiState) }

    private fun LibraryViewModel.sound(id: String) =
        uiState.value.categories.values.flatten().first { it.id == id }

    @Test
    fun soundsAreGroupedByCategory_andFirstCategoryIsSelected() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(setOf(SoundCategory.RAIN, SoundCategory.NATURE), state.categories.keys)
        assertEquals(SoundCategory.RAIN, state.selectedCategory)
    }

    @Test
    fun selectingACategory_showsIt() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectCategory(SoundCategory.NATURE)
        advanceUntilIdle()

        assertEquals(SoundCategory.NATURE, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun tappingADownloadedSound_playsIt_andTappingAgainStopsIt() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()
        assertTrue("rain" in viewModel.uiState.value.sceneCards.playingIds)
        assertEquals(1, playback.playCount)

        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()
        assertFalse("rain" in viewModel.uiState.value.sceneCards.playingIds)
    }

    @Test
    fun tappingASoundThatIsNotDownloaded_downloadsAndPlaysIt() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleSound(viewModel.sound("storm"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("storm" in state.sceneCards.playingIds)
        assertTrue(state.downloadingSoundIds.isEmpty())
        assertTrue(viewModel.sound("storm").isDownloaded)
    }

    @Test
    fun failedDownload_explainsWhy_andDoesNotPlay() = runMainTest {
        repository.onDownload = { false }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleSound(viewModel.sound("storm"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("storm" in state.sceneCards.playingIds)
        assertEquals(Res.string.download_failed, (state.userMessage as UiText.Resource).resId)
    }

    @Test
    fun changingVolume_updatesTheMixerAndTheSlider() = runMainTest {
        val viewModel = viewModel()
        observe(viewModel.volumes)
        advanceUntilIdle()
        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()

        viewModel.changeVolume("rain", 0.9f)
        advanceUntilIdle()

        assertEquals(mapOf("rain" to 0.9f), viewModel.volumes.value)
        assertEquals(0.9f, scene.scene.value.layers.single().volume)
    }

    @Test
    fun changingVolume_leavesTheScreenStateAlone() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.changeVolume("rain", 0.9f)
        advanceUntilIdle()

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun aPlayingEvent_showsItsFrequency_andItCanBeChanged() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.toggleSound(viewModel.sound("owl"))
        advanceUntilIdle()
        assertEquals(mapOf("owl" to EventFrequency.RARE), viewModel.uiState.value.sceneCards.frequencies)

        viewModel.changeFrequency("owl", EventFrequency.OFTEN)
        advanceUntilIdle()

        assertEquals(mapOf("owl" to EventFrequency.OFTEN), viewModel.uiState.value.sceneCards.frequencies)
    }

    @Test
    fun anEmptyLibrary_saysSo_andRetryFetchesTheCatalogAndStarterPack() = runMainTest {
        repository.sounds.value = emptyList()
        setup.isStarterPackInstalled.value = false
        val viewModel = viewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)

        repository.sounds.value = listOf(testSound("rain", isInitial = true))
        viewModel.retry()
        advanceUntilIdle()

        assertTrue(repository.syncCount > 0)
        assertEquals(listOf("rain"), repository.downloadRequests)
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun startingASoundIsCounted_stoppingItIsNot() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()
        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()

        assertEquals(
            listOf(AnalyticsEvent(EventTypes.SOUND_PLAY, mapOf("sound_id" to "rain", "category" to "RAIN", "source" to "BUILT_IN"))),
            analytics.events,
        )
    }
}
