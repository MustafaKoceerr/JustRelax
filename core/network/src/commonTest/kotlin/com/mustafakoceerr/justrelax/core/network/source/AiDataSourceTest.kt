package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.network.api.FakeApi
import com.mustafakoceerr.justrelax.core.network.api.jsonHeaders
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMix
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMixSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiSound
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiDataSourceTest {

    private val rain = NetworkAiSound("rain_light", "Light Rain", "RAIN", "BED", listOf("rain"))
    private val mixJson = """{"mix_name":"Rain","description":"d","sounds":[{"id":"rain_light","volume":0.6}]}"""
    private var sentBody = ""

    private fun api(status: HttpStatusCode = HttpStatusCode.OK, body: String = mixJson) = FakeApi { request ->
        sentBody = request.body.toByteArray().decodeToString()
        respond(body, status, jsonHeaders)
    }

    private fun FakeApi.dataSource() = ProxyAiDataSource(client, session, FakeApi.BASE_URL)

    @Test
    fun theProxyGetsTheWordsAndTheInventory_andTheMixComesBack() = runTest {
        val api = api()

        val result = api.dataSource().generateMix("rainy night", listOf(rain))

        assertEquals(Resource.Success(NetworkAiMix("Rain", "d", listOf(NetworkAiMixSound("rain_light", 0.6f)))), result)
        val request = api.calls.single()
        assertEquals("/v1/ai/mix", request.url.encodedPath)
        assertEquals("Bearer s1", request.headers[HttpHeaders.Authorization])
        val body = Json.parseToJsonElement(sentBody).jsonObject
        assertEquals("rainy night", body["prompt"]?.jsonPrimitive?.content)
        assertEquals("rain_light", body["sounds"]?.jsonArray?.single()?.jsonObject?.get("id")?.jsonPrimitive?.content)
    }

    @Test
    fun withoutDownloadedSoundsNothingIsSent() = runTest {
        val api = api()

        val result = api.dataSource().generateMix("rain", emptyList())

        assertIs<AppError.Ai.NoDownloadedSounds>((result as Resource.Error).error)
        assertTrue(api.calls.isEmpty())
    }

    @Test
    fun aLimitIsRateLimitedAndAProxyFailureIsAnApiError() = runTest {
        val limited = api(HttpStatusCode.TooManyRequests, "{}").dataSource().generateMix("rain", listOf(rain))
        val failed = api(HttpStatusCode.BadGateway, "{}").dataSource().generateMix("rain", listOf(rain))

        assertIs<AppError.Network.RateLimited>((limited as Resource.Error).error)
        assertEquals(502, ((failed as Resource.Error).error as AppError.Ai.ApiError).code)
    }
}
