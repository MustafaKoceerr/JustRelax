package com.mustafakoceerr.justrelax.feature.onboarding.navigation

import com.mustafakoceerr.justrelax.core.navigation.AppScreen

/**
 * Home modülünün dışarıya çıkmak için ihtiyaç duyduğu ekranlar.
 * Sadece Home'un ihtiyacı olanlar burada tanımlanır.
 */
interface OnboardingNavigator {
    fun toMain(): AppScreen
    // Yarın öbür gün: fun toDetail(id: String): AppScreen
}