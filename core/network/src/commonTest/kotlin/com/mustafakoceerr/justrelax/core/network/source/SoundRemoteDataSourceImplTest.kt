package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundClip
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SoundRemoteDataSourceImplTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val catalogUrl = "https://cdn.test/v2/sounds-v2.json"
    private val requestedUrls = mutableListOf<String>()

    private fun dataSource(handler: MockRequestHandler) = SoundRemoteDataSourceImpl(
        httpClient = createHttpClient(
            MockEngine { request ->
                requestedUrls += request.url.toString()
                handler(request)
            },
            enableLogging = false,
        ),
        catalogUrl = catalogUrl,
    )

    private val catalog = """
        {
          "version": 2,
          "sounds": [
            {
              "id": "birds",
              "names": {"en": "Birds"},
              "category": "NATURE",
              "icon": "BIRD",
              "kind": "EVENT",
              "tags": ["forest"],
              "default_frequency": "OFTEN",
              "license": "CC_BY_4_0",
              "attribution": {"title": "Dawn", "author": "someone", "url": "https://freesound.org/s/1"},
              "is_initial": true,
              "clips": [
                {"url": "audio/birds_0.ogg", "duration_ms": 3000, "size_bytes": 20000},
                {"url": "https://other.test/birds_1.ogg", "duration_ms": 2000, "size_bytes": 15000}
              ],
              "added_in_a_later_version": 1
            },
            {
              "id": "creek",
              "names": {"en": "Creek"},
              "category": "WATER",
              "kind": "BED",
              "license": "CC0",
              "clips": [{"url": "audio/creek.ogg", "duration_ms": 30000, "size_bytes": 270000}]
            }
          ]
        }
    """.trimIndent()

    @Test
    fun theV2Catalog_isFetchedAndParsed() = runTest {
        val sounds = dataSource { respond(catalog, headers = json) }.getSounds()

        assertEquals(listOf(catalogUrl), requestedUrls)
        assertEquals(listOf("birds", "creek"), sounds.map { it.id })
        val birds = sounds.first()
        assertEquals("EVENT", birds.kind)
        assertEquals("OFTEN", birds.defaultFrequency)
        assertEquals("someone", birds.attribution?.author)
        assertNull(sounds.last().icon)
    }

    @Test
    fun relativeClipUrls_areResolvedAgainstTheCatalogsDirectory() = runTest {
        val sounds = dataSource { respond(catalog, headers = json) }.getSounds()

        assertEquals(
            listOf(
                NetworkSoundClip("https://cdn.test/v2/audio/birds_0.ogg", 3000, 20000),
                NetworkSoundClip("https://other.test/birds_1.ogg", 2000, 15000),
            ),
            sounds.first().clips,
        )
    }

    @Test
    fun serverError_throwsServerError() = runTest {
        val dataSource = dataSource { respond("<html>oops</html>", HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<AppError.Network.ServerError> { dataSource.getSounds() }
        assertEquals(500, error.code)
    }

    @Test
    fun malformedJson_throwsSerializationError() = runTest {
        val dataSource = dataSource { respond("""[{"id":"v1 catalog"}]""", headers = json) }

        assertFailsWith<AppError.Network.SerializationError> { dataSource.getSounds() }
    }

    @Test
    fun connectionFailure_throwsNoInternet() = runTest {
        val dataSource = dataSource { throw IOException("unreachable") }

        assertFailsWith<AppError.Network.NoInternet> { dataSource.getSounds() }
    }
}
