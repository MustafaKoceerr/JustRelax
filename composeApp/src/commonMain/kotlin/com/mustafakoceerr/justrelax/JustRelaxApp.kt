package com.mustafakoceerr.justrelax

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import coil3.compose.setSingletonImageLoaderFactory
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.compositionlocal.LocalLanguageCode
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.core.ui.util.getAsyncImageLoader
import com.mustafakoceerr.justrelax.feature.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun JustRelaxApp() {
// 1. Coil Setup (Aynen kalıyor)
    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }

// 2. ViewModel ve Tema Yönetimi (BURASI DEĞİŞTİ)
    // Root seviyesinde olduğumuz için koinInject kullanıyoruz.
    val mainViewModel = koinInject<MainViewModel>()
    val currentTheme by mainViewModel.currentTheme.collectAsState()
    val currentLanguage by mainViewModel.currentLanguage.collectAsState() // <-- DİLİ DE DİNLE

    val isDarkTheme = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme() // Compose Multiplatform fonksiyonu
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    // 3. UI Başlatma
    // Temayı burada sarmalıyoruz. Artık Android/iOS fark etmeksizin çalışır.
    JustRelaxTheme(darkTheme = isDarkTheme) {

        CompositionLocalProvider(
            LocalLanguageCode provides currentLanguage.code
        ) {
            Navigator(
                screen = SplashScreen,
                key = currentLanguage.code
            ) { navigator ->
                FadeTransition(navigator)
            }
        }
    }
}