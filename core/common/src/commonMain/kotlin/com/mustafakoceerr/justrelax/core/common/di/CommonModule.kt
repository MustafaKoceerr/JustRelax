package com.mustafakoceerr.justrelax.core.common.di

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.common.dispatcher.StandardDispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
    singleOf(::StandardDispatchers) {
        bind<DispatcherProvider>()
    }
}