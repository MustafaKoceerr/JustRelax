package com.mustafakoceerr.justrelax.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.data.repository.StarterPackInstaller
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** Refreshes the catalog, starts the starter pack download on a fresh install, then opens the app. */
class SplashViewModel(
    private val soundRepository: SoundRepository,
    private val starterPackInstaller: StarterPackInstaller,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val start = TimeSource.Monotonic.markNow()

            // A failed sync is fine: the app works with the sounds it already has.
            soundRepository.syncIfStale()
            starterPackInstaller.install()

            val remaining = MIN_SPLASH_DURATION - start.elapsedNow()
            if (remaining.isPositive()) delay(remaining)

            _uiState.value = SplashUiState.Ready
        }
    }

    private companion object {
        val MIN_SPLASH_DURATION = 2.seconds
    }
}
