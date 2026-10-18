package com.mustafakoceerr.justrelax.core.network.api

import com.mustafakoceerr.justrelax.core.network.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.time.Clock

internal val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

/** A MockEngine API proxy that always grants session "s1"; [handler] answers the other calls. */
internal class FakeApi(
    private val handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
) {
    val calls = mutableListOf<HttpRequestData>()

    val client: HttpClient = createHttpClient(
        MockEngine { request ->
            if (request.url.encodedPath == "/v1/session") {
                respond("""{"token":"s1","expiresAt":${Long.MAX_VALUE / 2}}""", HttpStatusCode.OK, jsonHeaders)
            } else {
                calls += request
                handler(request)
            }
        },
        enableLogging = false,
    )

    val session = ApiSession(client, BASE_URL, { "install-1" }, { "integrity" }, Clock.System)

    companion object {
        const val BASE_URL = "https://api.test/"
    }
}
