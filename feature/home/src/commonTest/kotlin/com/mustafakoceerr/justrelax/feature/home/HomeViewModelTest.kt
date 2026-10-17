package com.mustafakoceerr.justrelax.feature.home

import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.testing.SoundLibraryEnvironment
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.download_failed
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeViewModelTest {

    private val library = SoundLibraryEnvironment(
        listOf(
            testSound("rain", localPath = "/sounds/rain.mp3", categoryId = "RAIN"),
            testSound("storm", categoryId = "RAIN"),
            testSound("forest", localPath = "/sounds/forest.mp3", categoryId = "NATURE"),
        )
    )
    private val mixer = FakeAudioMixer()

    private fun TestScope.viewModel() = HomeViewModel(
        getLocalizedCategorizedSoundsUseCase = GetLocalizedCategorizedSoundsUseCase(library.soundRepository, FakeLanguageController()),
        audioMixer = mixer,
        playSoundUseCase = PlaySoundUseCase(library.soundRepository, mixer),
        downloadSingleSoundUseCase = DownloadSingleSoundUseCase(library.storage, library.downloader, library.soundRepository),
    ).also { observe(it.uiState) }

    private fun HomeViewModel.sound(id: String) =
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
        library.downloader.onDownload = { false }
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
        assertEquals(0.9f, mixer.state.value.activeSounds.single().initialVolume)
    }
}
