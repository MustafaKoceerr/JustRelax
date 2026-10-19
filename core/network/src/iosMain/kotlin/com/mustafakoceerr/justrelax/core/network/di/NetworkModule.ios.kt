package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.network.api.AppAttestTokenProvider
import com.mustafakoceerr.justrelax.core.network.api.DeviceCheckAppAttestService
import com.mustafakoceerr.justrelax.core.network.api.FileAppAttestKeyStore
import com.mustafakoceerr.justrelax.core.network.api.IntegrityTokenProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Clock

/** App Attest proves to the API proxy that this is the genuine app on a genuine iPhone. */
internal actual val platformNetworkModule: Module = module {
    single<IntegrityTokenProvider> { AppAttestTokenProvider(DeviceCheckAppAttestService(), FileAppAttestKeyStore(), Clock.System) }
}
