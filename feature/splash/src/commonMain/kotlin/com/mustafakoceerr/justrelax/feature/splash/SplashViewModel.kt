package com.mustafakoceerr.justrelax.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsIfNecessaryUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class SplashViewModel(
    private val appSetupRepository: AppSetupRepository,
    private val syncSoundsIfNecessaryUseCase: SyncSoundsIfNecessaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val start = TimeSource.Monotonic.markNow()

            // A failed sync is fine: the app works with the sounds it already has.
            syncSoundsIfNecessaryUseCase()
            val isSetupFinished = appSetupRepository.isStarterPackInstalled.first()

            val remaining = MIN_SPLASH_DURATION - start.elapsedNow()
            if (remaining.isPositive()) delay(remaining)

            _uiState.value = SplashUiState.Ready(
                if (isSetupFinished) StartDestination.MAIN else StartDestination.ONBOARDING
            )
        }
    }

    private companion object {
        val MIN_SPLASH_DURATION = 2.seconds
    }
}
