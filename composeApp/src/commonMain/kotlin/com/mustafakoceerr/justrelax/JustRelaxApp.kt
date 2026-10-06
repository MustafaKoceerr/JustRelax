package com.mustafakoceerr.justrelax

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil3.compose.setSingletonImageLoaderFactory
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.compositionlocal.LocalLanguageCode
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.core.ui.util.getAsyncImageLoader
import com.mustafakoceerr.justrelax.feature.splash.SplashRoute
import com.mustafakoceerr.justrelax.feature.onboarding.OnboardingRoute
import com.mustafakoceerr.justrelax.feature.settings.SettingsRoute
import com.mustafakoceerr.justrelax.core.navigation.Route
import com.mustafakoceerr.justrelax.core.navigation.goBack
import com.mustafakoceerr.justrelax.core.navigation.navigate
import com.mustafakoceerr.justrelax.core.navigation.resetTo
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JustRelaxApp() {
    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }

    val mainViewModel = koinViewModel<MainViewModel>()
    val currentTheme by mainViewModel.currentTheme.collectAsState()

    val isDarkTheme = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    JustRelaxTheme(darkTheme = isDarkTheme) {
        JustRelaxNavDisplay()
    }
}

@Composable
private fun JustRelaxNavDisplay() {
    // Saved across configuration changes and process death; the serializers module is needed on iOS.
    val backStack = rememberNavBackStack(
        SavedStateConfiguration { serializersModule = Route.serializersModule },
        Route.Splash,
    )

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Scopes ViewModels to their destination; they are cleared when it leaves the back stack.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<Route.Splash> {
                SplashRoute(
                    onNavigateToMain = { backStack.resetTo(Route.Main) },
                    onNavigateToOnboarding = { backStack.resetTo(Route.Onboarding) },
                )
            }
            entry<Route.Onboarding> {
                OnboardingRoute(onFinished = { backStack.resetTo(Route.Main) })
            }
            entry<Route.Main> {
                MainRoute(onOpenSettings = { backStack.navigate(Route.Settings) })
            }
            entry<Route.Settings> {
                SettingsRoute(onBack = { backStack.goBack() })
            }
        },
    )
}
