package com.mustafakoceerr.justrelax

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.data.manager.SyncManager
import com.mustafakoceerr.justrelax.core.domain.manager.AppInitializationManager
import kotlinx.coroutines.launch

class MainViewModel (
    private val appInitializationManager: AppInitializationManager
): ScreenModel{
    // UI'ın dinleyeceği "Hazır mı?" durumu.
    val isInitialized = appInitializationManager.isInitialized

    init {
        // ViewModel oluşturulduğu an başlatma işlemini tetikle.
        screenModelScope.launch {
            appInitializationManager.initialize()
        }
    }
}