package com.mustafakoceerr.justrelax.feature.onboarding

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.SetAppSetupFinishedUseCase
import com.mustafakoceerr.justrelax.core.testing.SoundLibraryEnvironment
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingEffect
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingIntent
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingScreenStatus
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    private val library = SoundLibraryEnvironment(
        listOf(testSound("rain", isInitial = true), testSound("fire", isInitial = true), testSound("city"))
    )
    private val appSetup = FakeAppSetupRepository(installed = false)

    private fun viewModel() = OnboardingViewModel(
        syncSoundsUseCase = library.syncSounds,
        getSoundsUseCase = library.getSounds,
        downloadInitialSoundsUseCase = library.downloadInitial,
        downloadAllSoundsUseCase = library.downloadAll,
        setAppSetupFinishedUseCase = SetAppSetupFinishedUseCase(appSetup),
    )

    @Test
    fun knownSounds_showDownloadOptions() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(OnboardingScreenStatus.CHOOSING, state.status)
        assertEquals(2, state.initialOption?.soundCount)
        assertEquals(3, state.allOption?.soundCount)
    }

    @Test
    fun noSoundsAndNoInternet_showsNoInternet() = runMainTest {
        library.soundRepository.sounds.value = emptyList()
        library.syncRepository.result = Resource.Error(AppError.Network.NoInternet())

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(OnboardingScreenStatus.NO_INTERNET, viewModel.state.value.status)
    }

    @Test
    fun successfulDownload_finishesSetup_andNavigatesToMain() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(OnboardingIntent.DownloadInitial)

            assertEquals(OnboardingEffect.NavigateToMainScreen, awaitItem())
        }
        assertEquals(OnboardingScreenStatus.COMPLETED, viewModel.state.value.status)
        assertTrue(appSetup.isStarterPackInstalled.value)
    }

    @Test
    fun failedDownload_showsError_andLetsUserRetry() = runMainTest {
        library.downloader.onDownload = { url -> !url.contains("fire") }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(OnboardingIntent.DownloadInitial)

            assertIs<OnboardingEffect.ShowError>(awaitItem())
        }
        assertEquals(OnboardingScreenStatus.CHOOSING, viewModel.state.value.status)
        assertFalse(appSetup.isStarterPackInstalled.value)
    }
}
