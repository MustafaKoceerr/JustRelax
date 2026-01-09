package com.mustafakoceerr.justrelax.feature.splash.navigation

import com.mustafakoceerr.justrelax.core.navigation.AppScreen

interface SplashNavigator {
    fun toHome(): AppScreen
    fun toOnBoarding(): AppScreen
}