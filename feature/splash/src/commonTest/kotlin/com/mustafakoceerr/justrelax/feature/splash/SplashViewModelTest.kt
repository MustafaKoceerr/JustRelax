package com.mustafakoceerr.justrelax.feature.splash

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.StarterPackInstaller
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeConsentRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplashViewModelTest {

    private val repository = FakeSoundRepository(listOf(testSound("rain", isInitial = true)))
    private val setup = FakeAppSetupRepository(installed = false)
    private val consent = FakeConsentRepository(ConsentStatus.GRANTED)

    private fun TestScope.viewModel() = SplashViewModel(
        soundRepository = repository,
        starterPackInstaller = StarterPackInstaller(repository, setup, scope = this),
        consentRepository = consent,
    )

    @Test
    fun startsLoading() = runMainTest {
        val viewModel = viewModel()

        assertEquals(SplashUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun aFreshInstall_goesStraightToTheApp_whileTheStarterPackDownloads() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(SplashUiState.Ready(needsConsent = false), viewModel.uiState.value)
        assertEquals(listOf("rain"), repository.downloadRequests)
        assertTrue(setup.isStarterPackInstalled.value)
    }

    @Test
    fun failedSync_doesNotBlockStartup() = runMainTest {
        repository.syncResult = Resource.Error(AppError.Network.NoInternet())
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(SplashUiState.Ready(needsConsent = false), viewModel.uiState.value)
    }

    @Test
    fun withoutAnAnswerTheUserIsAskedFirst() = runMainTest {
        consent.consent.value = ConsentStatus.UNKNOWN
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(SplashUiState.Ready(needsConsent = true), viewModel.uiState.value)
    }

    @Test
    fun theStoredAnswerIsAppliedAtEveryStart() = runMainTest {
        viewModel()
        advanceUntilIdle()

        assertEquals(1, consent.appliedCount)
    }
}
