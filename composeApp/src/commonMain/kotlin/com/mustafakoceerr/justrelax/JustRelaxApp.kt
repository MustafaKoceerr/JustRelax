package com.mustafakoceerr.justrelax

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.splash.SplashRoute
import com.mustafakoceerr.justrelax.feature.settings.SettingsRoute
import com.mustafakoceerr.justrelax.feature.settings.consent.ConsentRoute
import com.mustafakoceerr.justrelax.feature.settings.credits.CreditsRoute
import com.mustafakoceerr.justrelax.feature.settings.support.SupportRoute
import com.mustafakoceerr.justrelax.feature.mysounds.MySoundsRoute
import com.mustafakoceerr.justrelax.feature.mysounds.editor.SoundEditorRoute
import com.mustafakoceerr.justrelax.feature.freesound.FreesoundRoute
import com.mustafakoceerr.justrelax.navigation.asEditorOrigin
import com.mustafakoceerr.justrelax.navigation.Route
import com.mustafakoceerr.justrelax.navigation.goBack
import com.mustafakoceerr.justrelax.navigation.navigate
import com.mustafakoceerr.justrelax.navigation.resetTo
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
import org.koin.compose.koinInject
import androidx.compose.runtime.LaunchedEffect
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.logScreenView
import com.mustafakoceerr.justrelax.navigation.screenName

@Composable
fun JustRelaxApp() {
    val mainViewModel = koinViewModel<MainViewModel>()
    val currentTheme by mainViewModel.theme.collectAsStateWithLifecycle()

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

    val analytics = koinInject<AnalyticsHelper>()
    val currentRoute = backStack.lastOrNull() as? Route
    LaunchedEffect(currentRoute) {
        // Main reports its tabs itself (MainScreen); its own view is not counted twice.
        if (currentRoute != null && currentRoute != Route.Main) analytics.logScreenView(currentRoute.screenName)
    }

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
                    onNavigateToConsent = { backStack.resetTo(Route.Consent) },
                )
            }
            entry<Route.Consent> {
                ConsentRoute(onDone = { backStack.resetTo(Route.Main) })
            }
            entry<Route.Main> {
                MainRoute(
                    onOpenSettings = { backStack.navigate(Route.Settings) },
                    onOpenMySounds = { backStack.navigate(Route.MySounds) },
                )
            }
            entry<Route.Settings> {
                SettingsRoute(
                    onBack = { backStack.goBack() },
                    onOpenCredits = { backStack.navigate(Route.Credits) },
                    onOpenSupport = { backStack.navigate(Route.Support) },
                )
            }
            entry<Route.Credits> {
                CreditsRoute(onBack = { backStack.goBack() })
            }
            entry<Route.Support> {
                SupportRoute(onBack = { backStack.goBack() })
            }
            entry<Route.MySounds> {
                MySoundsRoute(
                    onBack = { backStack.goBack() },
                    onOpenEditor = { backStack.navigate(Route.SoundEditor(it.path, it.displayName)) },
                    onOpenFreesound = { backStack.navigate(Route.Freesound) },
                )
            }
            entry<Route.Freesound> {
                FreesoundRoute(
                    onBack = { backStack.goBack() },
                    onOpenEditor = { backStack.navigate(Route.SoundEditor(it.path, it.name, it.origin.asEditorOrigin())) },
                )
            }
            entry<Route.SoundEditor> { route ->
                SoundEditorRoute(
                    route.sourcePath,
                    route.suggestedName,
                    route.origin?.asSoundOrigin(),
                    onBack = { backStack.goBack() },
                )
            }
        },
    )
}
