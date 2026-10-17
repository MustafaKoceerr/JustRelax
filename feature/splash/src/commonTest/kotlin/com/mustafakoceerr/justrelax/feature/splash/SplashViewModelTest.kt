package com.mustafakoceerr.justrelax.feature.splash

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.GetAppSetupStatusUseCase
import com.mustafakoceerr.justrelax.core.testing.SoundLibraryEnvironment
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.feature.splash.mvi.SplashEffect
import kotlin.test.Test
import kotlin.test.assertEquals

class SplashViewModelTest {

    private val library = SoundLibraryEnvironment()

    private fun viewModel(installed: Boolean) = SplashViewModel(
        getAppSetupStatusUseCase = GetAppSetupStatusUseCase(FakeAppSetupRepository(installed)),
        syncSoundsIfNecessaryUseCase = library.syncSoundsIfNecessary,
    )

    @Test
    fun setupFinished_navigatesToMain() = runMainTest {
        viewModel(installed = true).effect.test {
            assertEquals(SplashEffect.NavigateToMain, awaitItem())
        }
    }

    @Test
    fun setupNotFinished_navigatesToOnboarding() = runMainTest {
        viewModel(installed = false).effect.test {
            assertEquals(SplashEffect.NavigateToOnboarding, awaitItem())
        }
    }

    @Test
    fun failedSync_doesNotBlockStartup() = runMainTest {
        library.syncRepository.result = Resource.Error(AppError.Network.NoInternet())

        viewModel(installed = true).effect.test {
            assertEquals(SplashEffect.NavigateToMain, awaitItem())
        }
    }
}
