package com.mustafakoceerr.justrelax
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
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
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.main.MainScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import coil3.disk.DiskCache // <-- EKLENDİ
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider

@OptIn(InternalVoyagerApi::class)
@Composable
fun JustRelaxApp() {
// Koin'den Path Provider'ı alıyoruz
    val storageProvider: StoragePathProvider = koinInject()

    // Coil'e SVG Decoder'ı öğretiyoruz.
    setSingletonImageLoaderFactory { context->
        ImageLoader.Builder(context)
            .components {
                // 1. AĞ DESTEĞİ (Bunu unutmuştuk!)
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory()) // SVG Desteği
            }
            // --- DISK CACHE AYARLARI ---
            .diskCache {
                DiskCache.Builder()
                    .directory(storageProvider.getCacheDir().resolve("icon_cache"))// Cache klasörü içinde alt klasör
                    .maxSizeBytes(50L * 1024 * 1024) // Limit: 50 MB (İkonlar için çok bile)
                    .build()
            }
            .crossfade(true)// Resimler yumuşak gelsin
            .build()
    }
    val settingsRepository: SettingsRepository = koinInject()
    val currentTheme by settingsRepository.getTheme().collectAsState(initial = AppTheme.SYSTEM)

    val useDarkTheme = when(currentTheme){
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    JustRelaxTheme(darkTheme = useDarkTheme) {
        val appNavigator: AppNavigator = koinInject()

        Navigator(screen = MainScreen){ navigator ->
            LaunchedEffect(navigator.key){
                appNavigator.navigationEvents
                    .onEach { event-> event(navigator) }
                    .launchIn(this)
            }
            FadeTransition(navigator)
        }
    }
}