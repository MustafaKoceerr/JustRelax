package com.mustafakoceerr.justrelax.core.domain.manager

// Sadece ne yapılacağını söyleyen bir sözleşme (arayüz)
interface AppInitializer {
    suspend fun initializeApp()
}