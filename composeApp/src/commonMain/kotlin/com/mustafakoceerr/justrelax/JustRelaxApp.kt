package com.mustafakoceerr.justrelax
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.home.HomeScreen
import com.mustafakoceerr.justrelax.feature.main.MainScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject

@OptIn(InternalVoyagerApi::class)
@Composable
fun JustRelaxApp() {
    // ProvideLanguage SİLİNDİ.

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