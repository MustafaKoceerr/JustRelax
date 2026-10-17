package com.mustafakoceerr.justrelax.feature.onboarding

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    private val repository = FakeSoundRepository(
        listOf(testSound("rain", isInitial = true), testSound("fire", isInitial = true), testSound("city"))
    )
    private val appSetup = FakeAppSetupRepository(installed = false)

    private fun viewModel() = OnboardingViewModel(
        soundRepository = repository,
        appSetupRepository = appSetup,
    )

    @Test
    fun knownSounds_showDownloadOptions() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(OnboardingStatus.CHOOSING, state.status)
        assertEquals(2, state.starterPack?.soundCount)
        assertEquals(3, state.fullLibrary?.soundCount)
    }

    @Test
    fun noSoundsAndNoInternet_showsNoInternet_andRetryRecovers() = runMainTest {
        repository.sounds.value = emptyList()
        repository.syncResult = Resource.Error(AppError.Network.NoInternet())
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(OnboardingStatus.NO_INTERNET, viewModel.uiState.value.status)

        repository.syncResult = Resource.Success(Unit)
        repository.sounds.value = listOf(testSound("rain", isInitial = true))
        viewModel.retryLoadingConfig()
        advanceUntilIdle()

        assertEquals(OnboardingStatus.CHOOSING, viewModel.uiState.value.status)
    }

    @Test
    fun successfulDownload_finishesSetup() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.downloadStarterPack()
        advanceUntilIdle()

        assertEquals(OnboardingStatus.COMPLETED, viewModel.uiState.value.status)
        assertTrue(appSetup.isStarterPackInstalled.value)
    }

    @Test
    fun failedDownload_showsMessage_andLetsUserRetry() = runMainTest {
        repository.onDownload = { sound -> sound.id != "fire" }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.downloadStarterPack()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(OnboardingStatus.CHOOSING, state.status)
        assertNotNull(state.userMessage)
        assertFalse(appSetup.isStarterPackInstalled.value)

        viewModel.onMessageShown()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun downloadingFullLibrary_downloadsEverySound() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.downloadFullLibrary()
        advanceUntilIdle()

        assertEquals(3, repository.downloadRequests.size)
        assertEquals(OnboardingStatus.COMPLETED, viewModel.uiState.value.status)
    }
}
