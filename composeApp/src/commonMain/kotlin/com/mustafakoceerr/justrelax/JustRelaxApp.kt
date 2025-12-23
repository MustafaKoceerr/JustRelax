package com.mustafakoceerr.justrelax
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import coil3.disk.DiskCache
import com.mustafakoceerr.justrelax.core.domain.manager.StoragePathProvider
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme

@Composable
fun JustRelaxApp() {
// 1. Coil Setup (Aynen kalıyor)
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

// 2. ViewModel ve Tema Yönetimi (BURASI DEĞİŞTİ)
    // Root seviyesinde olduğumuz için koinInject kullanıyoruz.
    val mainViewModel = koinInject<MainViewModel>()
    val currentTheme by mainViewModel.currentTheme.collectAsState()

    val isDarkTheme = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme() // Compose Multiplatform fonksiyonu
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    // 3. UI Başlatma
    // Temayı burada sarmalıyoruz. Artık Android/iOS fark etmeksizin çalışır.
    JustRelaxTheme(darkTheme = isDarkTheme) {

        // Voyager Navigator
        Navigator(screen = MainScreen) { navigator ->
            FadeTransition(navigator)
        }
    }
}