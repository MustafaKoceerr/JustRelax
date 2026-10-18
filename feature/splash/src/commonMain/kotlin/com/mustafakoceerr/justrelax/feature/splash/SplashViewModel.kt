package com.mustafakoceerr.justrelax.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.ConsentRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import com.mustafakoceerr.justrelax.core.data.repository.StarterPackInstaller
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Refreshes the catalog, starts the starter pack download on a fresh install and applies the
 * analytics answer, then opens the app, or the analytics question if it was never answered.
 */
class SplashViewModel(
    private val soundRepository: SoundRepository,
    private val starterPackInstaller: StarterPackInstaller,
    private val consentRepository: ConsentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val start = TimeSource.Monotonic.markNow()
            consentRepository.applyStoredConsent()

            // A failed sync is fine: the app works with the sounds it already has.
            soundRepository.syncIfStale()
            starterPackInstaller.install()

            val remaining = MIN_SPLASH_DURATION - start.elapsedNow()
            if (remaining.isPositive()) delay(remaining)

            _uiState.value = SplashUiState.Ready(needsConsent = consentRepository.consent.first() == ConsentStatus.UNKNOWN)
        }
    }

    private companion object {
        val MIN_SPLASH_DURATION = 2.seconds
    }
}
