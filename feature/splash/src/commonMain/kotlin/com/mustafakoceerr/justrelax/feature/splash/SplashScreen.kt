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
import org.koin.compose.koinInject

object SplashScreen : AppScreen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<SplashViewModel>()
        val splashNavigator = koinInject<SplashNavigator>()

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

        JustRelaxBackground {
            LoadingScreen()
        }
    }
}