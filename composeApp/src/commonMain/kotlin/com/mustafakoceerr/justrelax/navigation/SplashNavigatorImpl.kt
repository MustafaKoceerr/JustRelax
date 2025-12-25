package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.MainScreen
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.onboarding.OnboardingScreen
import com.mustafakoceerr.justrelax.feature.splash.navigation.SplashNavigator

class SplashNavigatorImpl : SplashNavigator {
    override fun toHome(): AppScreen {
        // DÜZELTME: HomeScreen değil, MainScreen dönmeliyiz.
        // MainScreen içinde BottomBar ve TabNavigator var.
        return MainScreen
    }

    override fun toOnBoarding(): AppScreen {
        return OnboardingScreen
    }
}