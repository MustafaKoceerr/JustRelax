package com.mustafakoceerr.justrelax
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.feature.home.HomeScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject

@OptIn(InternalVoyagerApi::class)
@Composable
fun App() {
    // AppNavigator'ı doğrudan Koin'den enjekte ediyoruz.
    // KoinApplication sarmalayıcısı artık burada değil.
    val appNavigator: AppNavigator = koinInject()

    MaterialTheme {
        // Başlangıç ekranı olarak HomeScreen'i belirliyoruz.
        Navigator(screen = HomeScreen) { navigator ->

            // AppNavigator'dan gelen navigasyon olaylarını dinliyoruz.
            LaunchedEffect(navigator.key) {
                appNavigator.navigationEvents
                    .onEach { event -> event(navigator) }
                    .launchIn(this)
            }

            // Ekranlar arası geçiş animasyonunu burada belirliyoruz.
            SlideTransition(navigator)
        }
    }
}