package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import org.koin.dsl.module

val uiModule = module {
    single { GlobalSnackbarController() }
}