package com.mustafakoceerr.justrelax.feature.splash.mvi

// Splash ekranı sadece geçiş olduğu için State tutmasına gerek yok
// ama Voyager pattern'i bozulmasın diye boş bir state veya loading tutabiliriz.
data class SplashState(
    val isLoading: Boolean = true
)

sealed interface SplashEffect {
    data object NavigateToOnboarding : SplashEffect
    data object NavigateToMain : SplashEffect
}