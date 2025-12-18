package com.mustafakoceerr.justrelax.feature.timer.di

import com.mustafakoceerr.justrelax.feature.timer.TimerViewModel
import org.koin.dsl.module

val timerModule = module {
    // TimerManager zaten :core:audio modülünde (AudioModule) 'single' olarak tanımlı.
    // Koin 'get()' diyerek onu bulup ViewModel'e enjekte edecek.
    factory { TimerViewModel(get()) }
}