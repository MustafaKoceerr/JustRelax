package com.mustafakoceerr.justrelax.feature.splash

sealed interface SplashUiState {
    data object Loading : SplashUiState
    /** [needsConsent]: the user has not answered the analytics question yet, so it comes first. */
    data class Ready(val needsConsent: Boolean) : SplashUiState
}
