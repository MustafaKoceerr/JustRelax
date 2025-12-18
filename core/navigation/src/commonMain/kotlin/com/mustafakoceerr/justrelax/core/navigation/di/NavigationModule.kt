package com.mustafakoceerr.justrelax.core.navigation.di

import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import org.koin.dsl.module

val navigationModule = module {
    single { AppNavigator() }
}