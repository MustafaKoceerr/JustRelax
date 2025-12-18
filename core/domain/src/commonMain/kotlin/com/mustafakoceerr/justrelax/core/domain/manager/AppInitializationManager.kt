package com.mustafakoceerr.justrelax.core.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Artık SyncManager'ı değil, AppInitializer'ı tanıyor.
class AppInitializationManager(private val appInitializer: AppInitializer) {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    suspend fun initialize() {
        if (_isInitialized.value) return

        try {
            // Arayüz üzerinden çağırdığı için arkada ne çalıştığını bilmiyor.
            appInitializer.initializeApp()
        } catch (e: Exception) {
        } finally {
            _isInitialized.value = true
        }
    }
}