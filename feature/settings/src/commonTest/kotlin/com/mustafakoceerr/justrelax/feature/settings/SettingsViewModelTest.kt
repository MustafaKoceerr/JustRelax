package com.mustafakoceerr.justrelax.feature.settings

import com.mustafakoceerr.justrelax.core.domain.system.LanguageStrategy
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetLegalUrlUseCase
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.testing.SoundLibraryEnvironment
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLegalRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSystemLauncher
import com.mustafakoceerr.justrelax.core.testing.fake.FakeUserPreferencesRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsViewModelTest {

    private val preferences = FakeUserPreferencesRepository()
    private val launcher = FakeSystemLauncher()
    private val library = SoundLibraryEnvironment(listOf(testSound("rain"), testSound("fire")))

    private fun viewModel(strategy: LanguageStrategy = LanguageStrategy.IN_APP) = SettingsViewModel(
        preferences = preferences,
        soundRepository = library.soundRepository,
        downloadAllSoundsUseCase = library.downloadAll,
        getLegalUrlUseCase = GetLegalUrlUseCase(preferences, FakeLegalRepository()),
        systemLauncher = launcher,
        languageController = FakeLanguageController(strategy),
    )

    private fun TestScope.observedViewModel(strategy: LanguageStrategy = LanguageStrategy.IN_APP) =
        viewModel(strategy).also { observe(it.uiState) }

    @Test
    fun themeChange_isPersistedAndShown() = runMainTest {
        val viewModel = observedViewModel()

        viewModel.changeTheme(AppTheme.DARK)
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, viewModel.uiState.value.theme)
        assertEquals(AppTheme.DARK, preferences.theme.value)
    }

    @Test
    fun libraryAlreadyDownloaded_isShownAsComplete() = runMainTest {
        library.soundRepository.sounds.value = listOf(
            testSound("rain", localPath = "/sounds/rain.mp3"),
            testSound("fire", localPath = "/sounds/fire.mp3"),
        )

        val viewModel = observedViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLibraryComplete)
    }

    @Test
    fun downloadingLibrary_marksItComplete_andConfirms() = runMainTest {
        val viewModel = observedViewModel()

        viewModel.downloadLibrary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLibraryComplete)
        assertFalse(state.isDownloadingLibrary)
        assertIs<UiText.Resource>(state.userMessage)
    }

    @Test
    fun failedLibraryDownload_showsErrorAndAllowsRetry() = runMainTest {
        library.downloader.onDownload = { false }
        val viewModel = observedViewModel()

        viewModel.downloadLibrary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLibraryComplete)
        assertFalse(state.isDownloadingLibrary)
        assertIs<UiText.DynamicString>(state.userMessage)

        viewModel.onMessageShown()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun languageSelection_opensSheetInApp_andAppliesChoice() = runMainTest {
        val viewModel = observedViewModel()

        viewModel.openLanguageSelection()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLanguageSheetOpen)

        viewModel.changeLanguage(AppLanguage.TURKISH)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLanguageSheetOpen)
        assertEquals(AppLanguage.TURKISH, viewModel.uiState.value.language)
    }

    @Test
    fun languageSelection_onIos_opensSystemSettings() = runMainTest {
        observedViewModel(LanguageStrategy.SYSTEM_SETTINGS).openLanguageSelection()

        assertTrue(launcher.languageSettingsOpened)
    }

    @Test
    fun privacyPolicy_opensLocalizedUrl() = runMainTest {
        val viewModel = observedViewModel()

        viewModel.openPrivacyPolicy()
        advanceUntilIdle()

        assertTrue(launcher.openedUrls.single().endsWith("/privacy"))
    }
}
