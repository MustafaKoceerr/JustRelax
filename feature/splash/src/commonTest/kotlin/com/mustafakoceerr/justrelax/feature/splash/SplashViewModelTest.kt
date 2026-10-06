package com.mustafakoceerr.justrelax.feature.splash

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.testing.SoundLibraryEnvironment
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals

class SplashViewModelTest {

    private val library = SoundLibraryEnvironment()

    private fun viewModel(installed: Boolean) = SplashViewModel(
        appSetupRepository = FakeAppSetupRepository(installed),
        syncSoundsIfNecessaryUseCase = library.syncSoundsIfNecessary,
    )

    @Test
    fun startsLoading() = runMainTest {
        val viewModel = viewModel(installed = true)

        assertEquals(SplashUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun setupFinished_isReadyForMain() = runMainTest {
        val viewModel = viewModel(installed = true)
        advanceUntilIdle()

        assertEquals(SplashUiState.Ready(StartDestination.MAIN), viewModel.uiState.value)
    }

    @Test
    fun setupNotFinished_isReadyForOnboarding() = runMainTest {
        val viewModel = viewModel(installed = false)
        advanceUntilIdle()

        assertEquals(SplashUiState.Ready(StartDestination.ONBOARDING), viewModel.uiState.value)
    }

    @Test
    fun failedSync_doesNotBlockStartup() = runMainTest {
        library.syncRepository.result = Resource.Error(AppError.Network.NoInternet())
        val viewModel = viewModel(installed = true)
        advanceUntilIdle()

        assertEquals(SplashUiState.Ready(StartDestination.MAIN), viewModel.uiState.value)
    }
}
