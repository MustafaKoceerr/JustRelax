package com.mustafakoceerr.justrelax.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.splash.components.LoadingScreen
import com.mustafakoceerr.justrelax.feature.splash.mvi.SplashEffect
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
fun SplashRoute(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    val viewModel = koinViewModel<SplashViewModel>()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SplashEffect.NavigateToMain -> {
                    onNavigateToMain()
                }
                SplashEffect.NavigateToOnboarding -> {
                    onNavigateToOnboarding()
                }
            }
        }
    }

    SplashScreenContent()
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