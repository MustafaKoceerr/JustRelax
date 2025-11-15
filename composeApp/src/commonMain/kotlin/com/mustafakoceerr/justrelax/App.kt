package com.mustafakoceerr.justrelax

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// TODO: Koin ile sağlandığında bu satırı sileceğiz.
private val appNavigator = AppNavigator()
@OptIn(InternalVoyagerApi::class)
@Composable
fun App(){
    MaterialTheme {
        // TODO: Buraya ilk ekranımızı (örneğin HomeScreen) vereceğiz.
        // Şimdilik boş bir ekranla başlıyoruz.

        Navigator(screen = EmptyScreen()){navigator->
            // AppNavigator'dan gelen navigasyon olaylarını dinle
            LaunchedEffect(navigator.key){
                appNavigator.navigationEvents
                    .onEach { event-> event(navigator) }
                    .launchIn(this)
            }

            // Ekranlar arası geçiş animasyonunu burada belirliyoruz
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