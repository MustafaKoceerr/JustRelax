package com.mustafakoceerr.justrelax.feature.splash.navigation

import com.mustafakoceerr.justrelax.core.navigation.AppScreen

/**
 * Home modülünün dışarıya çıkmak için ihtiyaç duyduğu ekranlar.
 * Sadece Home'un ihtiyacı olanlar burada tanımlanır.
 */
interface SplashNavigator {
    fun toHome(): AppScreen
    fun toOnBoarding(): AppScreen
    // Yarın öbür gün: fun toDetail(id: String): AppScreen
}