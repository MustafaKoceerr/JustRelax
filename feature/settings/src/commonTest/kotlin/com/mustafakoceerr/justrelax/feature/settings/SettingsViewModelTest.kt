package com.mustafakoceerr.justrelax.feature.settings

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetLegalUrlUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.testing.SoundLibraryEnvironment
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLegalRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSystemLauncher
import com.mustafakoceerr.justrelax.core.testing.fake.FakeUserPreferencesRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsEffect
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsIntent
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SettingsViewModelTest {

    private val preferences = FakeUserPreferencesRepository()
    private val launcher = FakeSystemLauncher()
    private val library = SoundLibraryEnvironment(listOf(testSound("rain"), testSound("fire")))

    private fun viewModel() = SettingsViewModel(
        getAppThemeUseCase = GetAppThemeUseCase(preferences),
        setAppThemeUseCase = SetAppThemeUseCase(preferences),
        getAppLanguageUseCase = GetAppLanguageUseCase(preferences),
        setAppLanguageUseCase = SetAppLanguageUseCase(preferences),
        downloadAllSoundsUseCase = library.downloadAll,
        systemLauncher = launcher,
        languageController = FakeLanguageController(),
        getLegalUrlUseCase = GetLegalUrlUseCase(preferences, FakeLegalRepository()),
    )

    @Test
    fun changingTheme_isPersistedAndReflectedInState() = runMainTest {
        val viewModel = viewModel()

        viewModel.processIntent(SettingsIntent.ChangeTheme(AppTheme.DARK))
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, preferences.theme.value)
        assertEquals(AppTheme.DARK, viewModel.state.value.currentTheme)
    }

    @Test
    fun downloadingWholeLibrary_marksLibraryComplete() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(SettingsIntent.DownloadAllLibrary)

            assertIs<UiText.Resource>(assertIs<SettingsEffect.ShowMessage>(awaitItem()).message)
        }
        assertTrue(viewModel.state.value.isLibraryComplete)
        assertFalse(viewModel.state.value.isDownloadingLibrary)
    }

    @Test
    fun failedLibraryDownload_showsErrorAndAllowsRetry() = runMainTest {
        library.downloader.onDownload = { false }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(SettingsIntent.DownloadAllLibrary)

            assertIs<UiText.DynamicString>(assertIs<SettingsEffect.ShowMessage>(awaitItem()).message)
        }
        assertFalse(viewModel.state.value.isLibraryComplete)
        assertFalse(viewModel.state.value.isDownloadingLibrary)
    }

    @Test
    fun privacyPolicy_opensLocalizedUrl() = runMainTest {
        val viewModel = viewModel()

        viewModel.processIntent(SettingsIntent.OpenPrivacyPolicy)
        advanceUntilIdle()

        assertEquals(1, launcher.openedUrls.size)
        assertTrue(launcher.openedUrls.single().endsWith("/privacy"))
    }
}
