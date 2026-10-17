package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class HttpClientTest {

    @Test
    fun longRunningRequest_isNotCutOffByATotalRequestTimeout() = runTest {
        // A large sound file on a slow network can easily take longer than 30 seconds.
        val engine = MockEngine.create {
            dispatcher = StandardTestDispatcher(testScheduler) // virtual time
            addHandler {
                delay(90.seconds)
                respond("audio-bytes")
            }
        }
        val client = createHttpClient(engine, enableLogging = false)

        val body = client.get("https://cdn.test/sounds/rain.mp3").bodyAsText()

        assertEquals("audio-bytes", body)
    }
}
