package com.mustafakoceerr.justrelax.feature.home.navigation

import com.mustafakoceerr.justrelax.core.navigation.AppScreen

/**
 * Home modülünün dışarıya çıkmak için ihtiyaç duyduğu ekranlar.
 * Sadece Home'un ihtiyacı olanlar burada tanımlanır.
 */
interface HomeNavigator {
    fun toSettings(): AppScreen
    // Yarın öbür gün: fun toDetail(id: String): AppScreen
}