package com.mustafakoceerr.justrelax.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.splash.components.LoadingScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashRoute(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val ready = uiState as? SplashUiState.Ready ?: return@LaunchedEffect
        when (ready.destination) {
            StartDestination.MAIN -> onNavigateToMain()
            StartDestination.ONBOARDING -> onNavigateToOnboarding()
        }
    }

    SplashScreen()
}

@Composable
fun SplashScreen() {
    JustRelaxBackground {
        LoadingScreen()
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
