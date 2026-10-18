package com.mustafakoceerr.justrelax.feature.settings

import com.mustafakoceerr.justrelax.core.common.AppInfo
import com.mustafakoceerr.justrelax.core.testing.fake.FakeConsentRepository
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import com.mustafakoceerr.justrelax.core.data.language.LanguageStrategy
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.model.TipProduct
import com.mustafakoceerr.justrelax.core.model.TipTier
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeTipRepository
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
    private val repository = FakeSoundRepository(listOf(testSound("rain"), testSound("fire")))

    private val consent = FakeConsentRepository(ConsentStatus.DENIED)
    private val tips = FakeTipRepository()

    private fun viewModel(strategy: LanguageStrategy = LanguageStrategy.IN_APP) = SettingsViewModel(
        preferences = preferences,
        soundRepository = repository,
        legalRepository = FakeLegalRepository(),
        systemLauncher = launcher,
        languageController = FakeLanguageController(strategy),
        consentRepository = consent,
        tipRepository = tips,
        appInfo = AppInfo(versionName = "1.4.0"),
    )

    private fun TestScope.observedViewModel(strategy: LanguageStrategy = LanguageStrategy.IN_APP) =
        viewModel(strategy).also { observe(it.uiState) }

    @Test
    fun theSupportRow_showsOnlyWhenTheStoreOffersTips() = runMainTest {
        val viewModel = observedViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canTip)

        tips.products.value = listOf(TipProduct(TipTier.SMALL, "$1.99"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canTip)
    }

    @Test
    fun theVersion_isTheAppsOwn() = runMainTest {
        val viewModel = observedViewModel()
        advanceUntilIdle()

        assertEquals("1.4.0", viewModel.uiState.value.versionName)
    }

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
        repository.sounds.value = listOf(
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
        repository.onDownload = { false }
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

    @Test
    fun usageDataCanBeTurnedOnAndOffInSettings() = runMainTest {
        val viewModel = observedViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isUsageDataEnabled)

        viewModel.setUsageDataEnabled(true)
        advanceUntilIdle()

        assertEquals(ConsentStatus.GRANTED, consent.consent.value)
        assertTrue(viewModel.uiState.value.isUsageDataEnabled)
    }
}
