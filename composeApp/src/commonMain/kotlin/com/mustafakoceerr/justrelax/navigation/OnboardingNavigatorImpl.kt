package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.MainScreen
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.onboarding.navigation.OnboardingNavigator

class OnboardingNavigatorImpl : OnboardingNavigator {
    override fun toMain(): AppScreen {
        return MainScreen
    }
}