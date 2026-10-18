package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.api.IntegrityTokenProvider
import org.koin.core.module.Module
import org.koin.dsl.module

// iOS needs App Attest before the API proxy accepts it; until then AI and Freesound report an error.
internal actual val platformNetworkModule: Module = module {
    single<IntegrityTokenProvider> { IntegrityTokenProvider { throw AppError.Network.IntegrityFailed("App Attest is not set up yet") } }
}
