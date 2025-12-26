package com.mustafakoceerr.justrelax.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.splash.components.LoadingScreen
import com.mustafakoceerr.justrelax.feature.splash.mvi.SplashEffect
import com.mustafakoceerr.justrelax.feature.splash.navigation.SplashNavigator
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

// 1. ROUTE (Stateful): Navigasyon ve Logic Sorumlusu
data object SplashScreen : AppScreen {

    @Composable
    override fun Content() {
        // Dependency Injection
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<SplashViewModel>() // Voyager standardı
        val splashNavigator = koinInject<SplashNavigator>()

        // Side Effects (Navigasyon Kararları)
        LaunchedEffect(Unit) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    SplashEffect.NavigateToMain -> {
                        navigator.replaceAll(splashNavigator.toHome())
                    }

                    SplashEffect.NavigateToOnboarding -> {
                        navigator.replaceAll(splashNavigator.toOnBoarding())
                    }
                }
            }
        }

        SplashScreenContent()
    }
}

@Composable
fun SplashScreenContent() {
    JustRelaxBackground {
        LoadingScreen()
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent()
}