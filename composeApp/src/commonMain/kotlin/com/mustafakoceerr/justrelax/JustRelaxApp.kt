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
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.ui.theme.JustRelaxTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import coil3.disk.DiskCache // <-- EKLENDİ
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider

@Composable
fun JustRelaxApp() {
    // 1. Coil Setup (Resim Yükleyici)
    val storageProvider: StoragePathProvider = koinInject()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(storageProvider.getCacheDir().resolve("icon_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    // 2. Tema Ayarları
    val settingsRepository: SettingsRepository = koinInject()
    val currentTheme by settingsRepository.getTheme().collectAsState(initial = AppTheme.SYSTEM)

    val useDarkTheme = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    // 3. UI Başlatma
    JustRelaxTheme(darkTheme = useDarkTheme) {
        val appNavigator: AppNavigator = koinInject()

        // Voyager Navigator - Başlangıç: MainScreen
        Navigator(screen = MainScreen) { navigator ->
            // AppNavigator (ViewModel'den gelen emirler) ile Voyager'ı bağlıyoruz
            LaunchedEffect(navigator) {
                appNavigator.navigationEvents
                    .onEach { event -> event(navigator) }
                    .launchIn(this)
            }
            FadeTransition(navigator)
        }
    }
}