package com.mustafakoceerr.justrelax
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.home.HomeScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject

@OptIn(InternalVoyagerApi::class)
@Composable
fun App() {
    // 1. SettingsRepository'yi Koin'den enjekte et
    val settingsRepository: SettingsRepository = koinInject()

    // 2. Repository'den gelen tema akışını dinle ve state'e dönüştür
    val currentTheme by settingsRepository.getTheme().collectAsState(initial = AppTheme.SYSTEM)

    // 3. Mevcut tema durumuna göre darkTheme'in ne olacağına karar ver
    val useDarkTheme = when(currentTheme){
        AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    // 4. Uygulamanın tamamını yeni JustRelaxTheme'imiz ile sar
    JustRelaxTheme(darkTheme = useDarkTheme) {
        // AppNavigator'ı Koin'den enjekte ediyoruz.
        val appNavigator: AppNavigator = koinInject()

        Navigator(screen = HomeScreen){navigator->
            LaunchedEffect(navigator.key){
                appNavigator.navigationEvents
                    .onEach { event-> event(navigator) }
                    .launchIn(this)
            }
            SlideTransition(navigator)
        }
    }
}