package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

internal actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        // OkHttp'ye özel ayarları burada yapıyoruz
        config {
            retryOnConnectionFailure(true)
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(5, TimeUnit.SECONDS)
        }
    }
}