package com.mustafakoceerr.justrelax.feature.splash.mvi

sealed interface SplashEffect {
    data object NavigateToOnboarding : SplashEffect
    data object NavigateToMain : SplashEffect
}