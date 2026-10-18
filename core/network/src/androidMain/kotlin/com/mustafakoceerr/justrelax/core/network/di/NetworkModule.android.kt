package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.network.api.IntegrityTokenProvider
import com.mustafakoceerr.justrelax.core.network.api.PlayIntegrityTokenProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNetworkModule: Module = module {
    single<IntegrityTokenProvider> {
        PlayIntegrityTokenProvider(androidContext(), getProperty(PLAY_INTEGRITY_PROJECT_PROPERTY, 0L))
    }
}
