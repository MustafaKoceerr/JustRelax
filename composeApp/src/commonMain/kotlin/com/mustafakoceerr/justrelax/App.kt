package com.mustafakoceerr.justrelax

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.di.coreModule
import com.mustafakoceerr.justrelax.di.platformModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App(){
    // KoinApplication, Koin'i başlatır ve tüm alt composable'ların
    // koinInject() gibi fonksiyonları kullanmasını sağlar.
    KoinApplication(
        application = {
            // Koin'e kullanacağı modülleri (tarif defterlerini) veriyoruz.
        modules(coreModule, platformModule)
        }
    ){
        // Koin başlatıldıktan sonra ana uygulamamızı yüklüyoruz.
        MainAppContent()

    }
}

@OptIn(InternalVoyagerApi::class)
@Composable
private fun MainAppContent() {
    // AppNavigator'ı Koin'den enjekte ediyoruz.
    val appNavigator: AppNavigator = koinInject()

    MaterialTheme {
        Navigator(screen = EmptyScreen()) {navigator ->
            LaunchedEffect(navigator.key){
                appNavigator.navigationEvents
                    .onEach { event-> event(navigator) }
                    .launchIn(this)
            }
            SlideTransition(navigator)
        }
    }
}

// Geçiçi boş ekran
private class EmptyScreen: AppScreen{
    @Composable
    override fun Content() {
        // bos
    }
}