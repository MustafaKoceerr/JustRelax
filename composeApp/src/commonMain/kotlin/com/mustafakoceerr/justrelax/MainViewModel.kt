package com.mustafakoceerr.justrelax

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel (
    private val getAppThemeUseCase: GetAppThemeUseCase,
//    private val appInitializationManager: AppInitializationManager
): ScreenModel{
//    // UI'ın dinleyeceği "Hazır mı?" durumu.
//    val isInitialized = appInitializationManager.isInitialized
//
//    init {
//        // ViewModel oluşturulduğu an başlatma işlemini tetikle.
//        screenModelScope.launch {
//            appInitializationManager.initialize()
//        }
//    }

    // MainActivity'nin dinleyeceği tema akışı
    val currentTheme = getAppThemeUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)
}