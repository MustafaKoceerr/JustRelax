package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import com.mustafakoceerr.justrelax.core.network.mapper.NetworkSoundToDomainMapper
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.io.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SoundRemoteDataSourceImplTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun dataSource(handler: MockRequestHandler) = SoundRemoteDataSourceImpl(
        httpClient = createHttpClient(MockEngine(handler), enableLogging = false),
        soundMapper = NetworkSoundToDomainMapper(),
    )

    @Test
    fun validResponse_isMappedToSounds() = runTest {
        val dataSource = dataSource {
            respond(
                """[{"id":"rain","names":{"en":"Rain"},"category":"RAIN","icon_url":"i","audio_url":"a.mp3","version":1,"is_initial":true,"size_bytes":10}]""",
                headers = json,
            )
        }

        val sounds = dataSource.getSounds()

        assertEquals(listOf("rain"), sounds.map { it.id })
        assertEquals("a.mp3", sounds.single().remoteUrl)
    }

    @Test
    fun serverError_throwsServerError() = runTest {
        val dataSource = dataSource { respond("<html>oops</html>", HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<AppError.Network.ServerError> { dataSource.getSounds() }
        assertEquals(500, error.code)
    }

    @Test
    fun malformedJson_throwsSerializationError() = runTest {
        val dataSource = dataSource { respond("""{"not":"a list"}""", headers = json) }

        assertFailsWith<AppError.Network.SerializationError> { dataSource.getSounds() }
    }

    @Test
    fun connectionFailure_throwsNoInternet() = runTest {
        val dataSource = dataSource { throw IOException("unreachable") }

        assertFailsWith<AppError.Network.NoInternet> { dataSource.getSounds() }
    }
}
