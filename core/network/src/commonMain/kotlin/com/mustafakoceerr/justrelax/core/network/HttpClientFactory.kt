package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Koin property set by the app; logging is only enabled for debug builds. */
const val NETWORK_LOGGING_PROPERTY = "network.logging"

internal fun createHttpClient(engine: HttpClientEngine, enableLogging: Boolean) = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // No total request timeout: sound downloads can legitimately take minutes on slow networks.
    // A connection that stops sending data is still cut off by the socket timeout.
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000L
        socketTimeoutMillis = 30_000L
    }

    install(UserAgent) {
        agent = "JustRelax KMP App v1.0"
    }

    if (enableLogging) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    defaultRequest {
        header("Accept", "*/*")
    }
}
