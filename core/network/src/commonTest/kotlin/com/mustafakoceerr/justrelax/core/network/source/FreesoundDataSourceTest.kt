package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.api.FakeApi
import com.mustafakoceerr.justrelax.core.network.api.jsonHeaders
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreesoundDataSourceTest {

    private val page = """
        {
          "count": 31,
          "next": "more",
          "results": [
            {
              "id": 123, "name": "Kettle boiling", "username": "someone",
              "license": "https://creativecommons.org/licenses/by/4.0/",
              "url": "https://freesound.org/people/someone/sounds/123/",
              "tags": ["kettle", "kitchen"], "duration": 12.5,
              "previews": {"preview-hq-ogg": "https://cdn.freesound.org/previews/0/123-hq.ogg"}
            }
          ]
        }
    """.trimIndent()

    private fun api(status: HttpStatusCode = HttpStatusCode.OK, body: String = page) =
        FakeApi { respond(body, status, jsonHeaders) }

    private fun FakeApi.dataSource() = ProxyFreesoundDataSource(client, session, FakeApi.BASE_URL)

    @Test
    fun theSearchGoesThroughTheProxyWithTheSessionAndNoKey() = runTest {
        val api = api()

        api.dataSource().search("  kettle ", page = 2)

        val request = api.calls.single()
        assertEquals("/v1/freesound/search", request.url.encodedPath)
        assertEquals("kettle", request.url.parameters["query"])
        assertEquals("2", request.url.parameters["page"])
        assertEquals("Bearer s1", request.headers[HttpHeaders.Authorization])
        assertNull(request.url.parameters["token"])
    }

    @Test
    fun aPageIsReadWithItsSoundsAndWhetherMoreFollow() = runTest {
        val result = api().dataSource().search("kettle", page = 1)

        assertTrue(result.hasMore)
        val sound = result.sounds.single()
        assertEquals(123, sound.id)
        assertEquals("someone", sound.username)
        assertEquals(12.5, sound.duration)
        assertEquals("https://cdn.freesound.org/previews/0/123-hq.ogg", sound.previews["preview-hq-ogg"])
    }

    @Test
    fun theLastPageHasNoMore() = runTest {
        assertFalse(api(body = """{"count": 1, "next": null, "results": []}""").dataSource().search("kettle", 1).hasMore)
    }

    @Test
    fun tooManyRequestsIsRateLimitedAndOtherFailuresAreServerErrors() = runTest {
        assertFailsWith<AppError.Network.RateLimited> { api(HttpStatusCode.TooManyRequests, "{}").dataSource().search("kettle", 1) }
        assertFailsWith<AppError.Network.ServerError> { api(HttpStatusCode.BadGateway, "{}").dataSource().search("kettle", 1) }
    }
}
