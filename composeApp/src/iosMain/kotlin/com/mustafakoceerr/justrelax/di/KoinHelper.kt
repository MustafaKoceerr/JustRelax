package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.core.network.di.API_BASE_URL_PROPERTY
import com.mustafakoceerr.justrelax.core.network.di.PRODUCTION_API_BASE_URL
import com.mustafakoceerr.justrelax.di.initKoin as initCommonKoin

/** Starts Koin; called once from `iOSApp.init` in Swift. */
fun initKoin() {
    initCommonKoin(platformModules = listOf(iosPlatformModule)) {
        properties(
            mapOf(
                NETWORK_LOGGING_PROPERTY to isDebugBuild,
                API_BASE_URL_PROPERTY to if (isDebugBuild) DEBUG_API_BASE_URL else PRODUCTION_API_BASE_URL,
            ),
        )
    }
}
