package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun createHttpClientEngine(): HttpClientEngine {
    return Darwin.create {
        // iOS'e (Darwin) özel ayarlar burada yapılır
        configureRequest {
            // Örneğin, hücresel veride de çalışmasına izin ver
            setAllowsCellularAccess(true)
        }
    }
}