package com.mustafakoceerr.justrelax.feature.library

import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.library.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import justrelax.feature.library.generated.resources.Res
import justrelax.feature.library.generated.resources.download_failed
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

    private fun TestScope.viewModel() = LibraryViewModel(
        getLocalizedCategorizedSoundsUseCase = GetLocalizedCategorizedSoundsUseCase(repository, FakeLanguageController()),
        sceneRepository = scene,
        playbackController = playback,
        soundRepository = repository,
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
        assertTrue("rain" in viewModel.uiState.value.playingSoundIds)
        assertEquals(1, playback.playCount)

        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()
        assertFalse("rain" in viewModel.uiState.value.playingSoundIds)
    }

    @Test
    fun tappingASoundThatIsNotDownloaded_downloadsAndPlaysIt() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleSound(viewModel.sound("storm"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("storm" in state.playingSoundIds)
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
        assertFalse("storm" in state.playingSoundIds)
        assertEquals(Res.string.download_failed, (state.userMessage as UiText.Resource).resId)
    }

    @Test
    fun changingVolume_updatesTheMixerAndTheSlider() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.toggleSound(viewModel.sound("rain"))
        advanceUntilIdle()

        viewModel.changeVolume("rain", 0.9f)
        advanceUntilIdle()

        assertEquals(0.9f, viewModel.uiState.value.soundVolumes["rain"])
        assertEquals(0.9f, scene.scene.value.layers.single().volume)
    }

    @Test
    fun aPlayingEvent_showsItsFrequency_andItCanBeChanged() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.toggleSound(viewModel.sound("owl"))
        advanceUntilIdle()
        assertEquals(mapOf("owl" to EventFrequency.RARE), viewModel.uiState.value.soundFrequencies)

        viewModel.changeFrequency("owl", EventFrequency.OFTEN)
        advanceUntilIdle()

        assertEquals(mapOf("owl" to EventFrequency.OFTEN), viewModel.uiState.value.soundFrequencies)
    }
}
