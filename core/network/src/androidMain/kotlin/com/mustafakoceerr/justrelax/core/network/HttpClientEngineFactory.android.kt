package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import java.util.concurrent.TimeUnit

internal actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            // 1. Timeoutları UZAT (Hücresel için şart)
            retryOnConnectionFailure(true)
            connectTimeout(15, TimeUnit.SECONDS) // Bağlantı kurma süresi
            readTimeout(30, TimeUnit.SECONDS)    // Veri okuma süresi (İndirme için kritik)
            writeTimeout(30, TimeUnit.SECONDS)

            // 2. TLS 1.2 Zorlaması (Cloudflare ayarınla eşleşsin)
            val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2) // Sadece TLS 1.2 ve üzerini kabul et
                .build()

            // Hem Modern (TLS 1.2+) hem de uyumluluk modu (Cleartext kapalı)
            connectionSpecs(listOf(tlsSpec, ConnectionSpec.CLEARTEXT))
        }
    }
}