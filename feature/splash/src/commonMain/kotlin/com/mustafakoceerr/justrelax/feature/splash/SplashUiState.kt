package com.mustafakoceerr.justrelax.feature.splash

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object Ready : SplashUiState
}
