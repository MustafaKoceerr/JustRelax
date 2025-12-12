package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import org.koin.core.module.Module
import org.koin.dsl.module

// Platforma özel implementasyonları bekliyoruz
expect val platformUiModule: Module

// 1. ORTAK UI MODÜLÜ (Her iki platformda AYNEN çalışanlar)
// GlobalSnackbarController saf Kotlin/Compose olduğu için buraya girer.
val sharedUiModule = module {
    // Singleton olarak tanımlıyoruz, tüm uygulama tek bir snackbar yöneticisi kullanacak.
    single { GlobalSnackbarController() }
}
