package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.network.api.ApiSession
import com.mustafakoceerr.justrelax.core.network.api.networkCall
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMix
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiSound
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

/** Asks a language model for a mix made only of the sounds in [inventory]. */
interface AiNetworkDataSource {
    suspend fun generateMix(prompt: String, inventory: List<NetworkAiSound>): Resource<NetworkAiMix>
}

/**
 * Asks through the API proxy, which holds the OpenAI key, the model and the instructions; the app
 * only sends the user's words and the sounds it has.
 */
internal class ProxyAiDataSource(
    private val httpClient: HttpClient,
    private val session: ApiSession,
    private val baseUrl: String,
) : AiNetworkDataSource {

    override suspend fun generateMix(prompt: String, inventory: List<NetworkAiSound>): Resource<NetworkAiMix> {
        if (inventory.isEmpty()) return Resource.Error(AppError.Ai.NoDownloadedSounds())
        return try {
            val response = session.authorized { bearer ->
                networkCall {
                    httpClient.post("${baseUrl}v1/ai/mix") {
                        header(HttpHeaders.Authorization, "Bearer $bearer")
                        contentType(ContentType.Application.Json)
                        setBody(MixRequest(prompt, inventory))
                    }
                }
            }
            when {
                response.status == HttpStatusCode.TooManyRequests -> Resource.Error(AppError.Network.RateLimited())
                !response.status.isSuccess() ->
                    Resource.Error(AppError.Ai.ApiError(response.status.value, "The AI proxy answered ${response.status}"))
                else -> Resource.Success(response.body<NetworkAiMix>())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError) {
            Resource.Error(e)
        } catch (e: Exception) {
            Resource.Error(AppError.Ai.EmptyResponse())
        }
    }

    @Serializable
    private data class MixRequest(val prompt: String, val sounds: List<NetworkAiSound>)
}
