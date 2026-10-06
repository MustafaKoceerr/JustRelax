package com.mustafakoceerr.justrelax.feature.splash

enum class StartDestination { MAIN, ONBOARDING }

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Ready(val destination: StartDestination) : SplashUiState
}
