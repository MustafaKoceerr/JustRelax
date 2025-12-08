package com.mustafakoceerr.justrelax

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.data.manager.SyncManager
import kotlinx.coroutines.launch

class MainViewModel (
    private val syncManager: SyncManager
): ScreenModel{
    init {
        // ViewModel oluşturulduğu an (Uygulama açılışında) işlemi başlat.
        screenModelScope.launch {
            syncManager.initializeApp()
        }
    }
}