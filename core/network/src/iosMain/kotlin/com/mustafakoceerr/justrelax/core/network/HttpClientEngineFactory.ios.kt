package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun createHttpClientEngine(): HttpClientEngine {
    return Darwin.create {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}