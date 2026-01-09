package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal val KtorClient = HttpClient(createHttpClientEngine()) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000L
    }

    install(UserAgent) {
        agent = "JustRelax KMP App v1.0"
    }

    install(Logging) {
        level = LogLevel.HEADERS
    }

    defaultRequest {
        header("Accept", "*/*")
    }
}