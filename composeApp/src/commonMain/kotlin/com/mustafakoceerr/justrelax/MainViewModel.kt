package com.mustafakoceerr.justrelax

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val getAppThemeUseCase: GetAppThemeUseCase,
//    private val appInitializationManager: AppInitializationManager
    private val syncSoundsIfNecessaryUseCase: SyncSoundsIfNecessaryUseCase
) : ScreenModel {
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

    init {
        // Uygulama başladığında (Root seviyesinde) Sync işlemini tetikle.
        // Logic (24 saat kontrolü vs.) tamamen UseCase'in içinde, ViewModel sadece "Başlat" der.
        screenModelScope.launch {
            try {
                syncSoundsIfNecessaryUseCase()
            } catch (e: Exception) {
                // İnternet yoksa veya hata olursa sessizce devam et,
                // uygulama offline modda (var olan verilerle) çalışsın.
            }
        }
    }
}

