package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformUiModule: Module

val uiModule = module {
    includes(platformUiModule)

    single { GlobalSnackbarController() }
}
