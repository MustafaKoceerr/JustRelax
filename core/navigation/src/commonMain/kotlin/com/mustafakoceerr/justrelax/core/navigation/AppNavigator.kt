package com.mustafakoceerr.justrelax.core.navigation

import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * ViewModel'lerin UI'dan bağımsız navigasyon emri vermesini sağlar.
 */
class AppNavigator {
    private val _navigationEvents = MutableSharedFlow<Navigator.()-> Unit>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    suspend fun navigate(screen: AppScreen){
        _navigationEvents.emit { push(screen) }
    }

    suspend fun pop(){
        _navigationEvents.emit { pop() }
    }

    suspend fun popToRoot(){
        _navigationEvents.emit { popUntilRoot() }
    }
}