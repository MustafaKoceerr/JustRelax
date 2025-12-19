package com.mustafakoceerr.justrelax.core.common.di

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.common.dispatcher.StandardDispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Bu modül, uygulama genelindeki temel ve ortak hizmetleri
 * (Dispatcher'lar gibi) Koin'e sağlar.
 */
val commonModule = module {
    // DispatcherProvider arayüzü istendiğinde,
    // StandardDispatchers nesnesini oluşturup ver.
    singleOf(::StandardDispatchers) {
        bind<DispatcherProvider>()
    }
}