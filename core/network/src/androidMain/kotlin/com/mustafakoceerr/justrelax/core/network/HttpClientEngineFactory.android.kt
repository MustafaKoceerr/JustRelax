package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import java.util.concurrent.TimeUnit

internal actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            retryOnConnectionFailure(true)
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)

            val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build()

            connectionSpecs(listOf(tlsSpec, ConnectionSpec.CLEARTEXT))
        }
    }
}