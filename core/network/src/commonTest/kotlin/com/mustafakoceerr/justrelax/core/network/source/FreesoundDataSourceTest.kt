package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreesoundDataSourceTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(apiKey: String = "secret", handler: MockRequestHandler) = KtorFreesoundDataSource(
        httpClient = createHttpClient(
            MockEngine { request ->
                requests += request
                handler(request)
            },
            enableLogging = false,
        ),
        apiKey = apiKey,
    )

    private val page = """
        {
          "count": 31,
          "next": "https://freesound.org/apiv2/search/?page=2",
          "results": [
            {
              "id": 123,
              "name": "Kettle boiling",
              "username": "someone",
              "license": "https://creativecommons.org/licenses/by/4.0/",
              "url": "https://freesound.org/people/someone/sounds/123/",
              "tags": ["kettle", "kitchen"],
              "duration": 12.5,
              "previews": {"preview-hq-ogg": "https://cdn.freesound.org/previews/0/123-hq.ogg", "preview-hq-mp3": "https://cdn.freesound.org/previews/0/123-hq.mp3"}
            }
          ]
        }
    """.trimIndent()

    @Test
    fun theSearchSendsTheQueryAndTheKeyAsAHeaderNotInTheUrl() = runTest {
        dataSource { respond(page, HttpStatusCode.OK, json) }.search("  kettle ", page = 2)

        val request = requests.single()
        assertEquals("/apiv2/search/", request.url.encodedPath)
        assertEquals("kettle", request.url.parameters["query"])
        assertEquals("2", request.url.parameters["page"])
        assertEquals("30", request.url.parameters["page_size"])
        assertEquals("license:(\"Creative Commons 0\" OR \"Attribution\") duration:[1 TO 300]", request.url.parameters["filter"])
        assertEquals("id,name,username,license,url,tags,duration,previews", request.url.parameters["fields"])
        assertNull(request.url.parameters["token"])
        assertEquals("Token secret", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun aPageIsReadWithItsSoundsAndWhetherMoreFollow() = runTest {
        val result = dataSource { respond(page, HttpStatusCode.OK, json) }.search("kettle", page = 1)

        assertTrue(result.hasMore)
        val sound = result.sounds.single()
        assertEquals(123, sound.id)
        assertEquals("Kettle boiling", sound.name)
        assertEquals("someone", sound.username)
        assertEquals("https://creativecommons.org/licenses/by/4.0/", sound.license)
        assertEquals(12.5, sound.duration)
        assertEquals("https://cdn.freesound.org/previews/0/123-hq.ogg", sound.previews["preview-hq-ogg"])
    }

    @Test
    fun theLastPageHasNoMore() = runTest {
        val last = """{"count": 1, "next": null, "results": []}"""

        assertFalse(dataSource { respond(last, HttpStatusCode.OK, json) }.search("kettle", page = 1).hasMore)
    }

    @Test
    fun tooManyRequestsIsReportedAsRateLimited() = runTest {
        assertFailsWith<AppError.Network.RateLimited> {
            dataSource { respond("", HttpStatusCode.TooManyRequests) }.search("kettle", page = 1)
        }
    }

    @Test
    fun withoutAKeyNothingIsSent() = runTest {
        assertFailsWith<AppError.Network.ServerError> {
            dataSource(apiKey = "") { respond(page, HttpStatusCode.OK, json) }.search("kettle", page = 1)
        }
        assertTrue(requests.isEmpty())
    }
}
