package com.mustafakoceerr.justrelax.core.network.api

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import com.mustafakoceerr.justrelax.core.testing.fake.FakeClock
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ApiSessionTest {

    private val requests = mutableListOf<HttpRequestData>()
    private val bodies = mutableListOf<String>()
    private val clock = FakeClock(Instant.fromEpochMilliseconds(1_800_000_000_000L))
    private val hashes = mutableListOf<String>()
    private var integrity = IntegrityTokenProvider { hash -> hashes += hash; "integrity-token" }
    private var sessionStatus = HttpStatusCode.OK
    private var issued = 0

    private val client = createHttpClient(
        MockEngine { request ->
            requests += request
            bodies += request.body.toByteArray().decodeToString()
            when (request.url.encodedPath) {
                "/v1/session" -> if (sessionStatus == HttpStatusCode.OK) {
                    issued++
                    respond("""{"token":"session-$issued","expiresAt":${(clock.now() + 1.hours).toEpochMilliseconds()}}""", HttpStatusCode.OK, jsonHeaders)
                } else {
                    respond("""{"error":"x"}""", sessionStatus, jsonHeaders)
                }
                else -> if (request.headers[HttpHeaders.Authorization] == "Bearer session-1" && issued == 1 && staleFirstSession) {
                    respond("""{"error":"unauthorized"}""", HttpStatusCode.Unauthorized, jsonHeaders)
                } else {
                    respond("{}", HttpStatusCode.OK, jsonHeaders)
                }
            }
        },
        enableLogging = false,
    )
    private var staleFirstSession = false

    private fun session() = ApiSession(client, "https://api.test/", { "install-1" }, integrity, clock)

    @Test
    fun theRequestHashIsTheSameAsTheServers() {
        // Same vector as server/api-proxy test/integrity.test.ts.
        assertEquals("b19f76277ff87a14d93396b5552ae5b71806ab94625a5a44fd0db9a917eebee6", sessionRequestHash("install-1"))
    }

    @Test
    fun aSessionIsCreatedWithAnIntegrityTokenForThisInstall() = runTest {
        assertEquals("session-1", session().bearer())

        assertEquals(listOf(sessionRequestHash("install-1")), hashes)
        assertEquals("https://api.test/v1/session", requests.single().url.toString())
        val body = Json.parseToJsonElement(bodies.single()).jsonObject
        assertEquals("install-1", body["installId"]?.jsonPrimitive?.content)
        assertEquals("integrity-token", body["integrityToken"]?.jsonPrimitive?.content)
    }

    @Test
    fun theSessionIsReusedUntilAMinuteBeforeItExpires() = runTest {
        val session = session()
        session.bearer()

        clock.advanceBy(1.hours - 61.seconds)
        assertEquals("session-1", session.bearer())
        clock.advanceBy(2.seconds)
        assertEquals("session-2", session.bearer())
        assertEquals(2, hashes.size)
    }

    @Test
    fun anAuthorizedCallThatIsRefusedRetriesOnceWithANewSession() = runTest {
        staleFirstSession = true
        val session = session()

        val response: HttpResponse = session.authorized { bearer ->
            client.get("https://api.test/v1/freesound/search") { header(HttpHeaders.Authorization, "Bearer $bearer") }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf("/v1/session", "/v1/freesound/search", "/v1/session", "/v1/freesound/search"), requests.map { it.url.encodedPath })
    }

    @Test
    fun aRefusedIntegrityCheckAndAFailingProviderAreIntegrityErrors_andALimitIsRateLimited() = runTest {
        sessionStatus = HttpStatusCode.Forbidden
        assertFailsWith<AppError.Network.IntegrityFailed> { session().bearer() }

        sessionStatus = HttpStatusCode.TooManyRequests
        assertFailsWith<AppError.Network.RateLimited> { session().bearer() }

        integrity = IntegrityTokenProvider { throw AppError.Network.IntegrityFailed("no Play services") }
        assertFailsWith<AppError.Network.IntegrityFailed> { session().bearer() }
    }
}
