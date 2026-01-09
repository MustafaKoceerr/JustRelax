package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import com.mustafakoceerr.justrelax.feature.onboarding.navigation.OnboardingNavigator
import com.mustafakoceerr.justrelax.feature.splash.navigation.SplashNavigator
import com.mustafakoceerr.justrelax.navigation.HomeNavigatorImpl
import com.mustafakoceerr.justrelax.navigation.OnboardingNavigatorImpl
import com.mustafakoceerr.justrelax.navigation.SplashNavigatorImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val navigationTargetsModule = module {
    singleOf(::HomeNavigatorImpl) { bind<HomeNavigator>() }
    singleOf(::OnboardingNavigatorImpl) { bind<OnboardingNavigator>() }
    singleOf(::SplashNavigatorImpl) { bind<SplashNavigator>() }
}