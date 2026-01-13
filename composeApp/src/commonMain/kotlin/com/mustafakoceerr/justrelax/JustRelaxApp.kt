package com.mustafakoceerr.justrelax

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import coil3.compose.setSingletonImageLoaderFactory
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.compositionlocal.LocalLanguageCode
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.core.ui.util.getAsyncImageLoader
import com.mustafakoceerr.justrelax.feature.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun JustRelaxApp() {
    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }

    val mainViewModel = koinInject<MainViewModel>()
    val currentTheme by mainViewModel.currentTheme.collectAsState()

    val isDarkTheme = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    JustRelaxTheme(darkTheme = isDarkTheme) {

            Navigator(
                screen = SplashScreen,
            ) { navigator ->
                SlideTransition(navigator)
            }
    }
}