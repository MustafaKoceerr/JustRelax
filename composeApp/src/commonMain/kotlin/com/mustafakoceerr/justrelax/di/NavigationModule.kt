package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import com.mustafakoceerr.justrelax.navigation.HomeNavigatorImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val navigationTargetsModule = module {
    // HomeNavigator istendiğinde HomeNavigatorImpl ver.
    // 'single' yapıyoruz çünkü navigator stateless bir yardımcı sınıftır, her seferinde üretmeye gerek yok.
    singleOf(::HomeNavigatorImpl) {
        bind<HomeNavigator>()
    }
}