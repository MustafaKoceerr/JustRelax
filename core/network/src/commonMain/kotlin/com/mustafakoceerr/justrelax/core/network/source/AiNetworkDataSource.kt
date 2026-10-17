package com.mustafakoceerr.justrelax.core.network.source

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.network.BuildConfig
import com.mustafakoceerr.justrelax.core.network.createHttpClientEngine
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMix
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiSound
import kotlinx.serialization.json.Json

/** Asks a language model for a mix made only of the sounds in [inventory]. */
interface AiNetworkDataSource {
    suspend fun generateMix(prompt: String, inventory: List<NetworkAiSound>): Resource<NetworkAiMix>
}

internal class OpenAiNetworkDataSource : AiNetworkDataSource {

    // The key is still built into the app; it moves to a server before the next release.
    private val openai = OpenAI(OpenAIConfig(token = BuildConfig.OPENAI_API_KEY, engine = createHttpClientEngine()))
    private val modelName = "gpt-4o-mini"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun generateMix(
        prompt: String,
        inventory: List<NetworkAiSound>
    ): Resource<NetworkAiMix> {
        return try {
            if (inventory.isEmpty()) {
                return Resource.Error(AppError.Ai.NoDownloadedSounds())
            }

            val systemInstruction = """
                You are an expert ambient sound designer building a soundscape.

                ### INVENTORY (JSON):
                ${json.encodeToString(inventory)}

                Each sound has a kind:
                - BED: a continuous loop that carries the scene (rain, wind, a stream, noise).
                - EVENT: short sounds that occur at random moments (a bird, an owl, a crackle, thunder).
                  For events choose a frequency: RARE, NORMAL or OFTEN.

                ### RULES:
                1. Use ONLY ids from the inventory; pick sounds whose name, category and tags fit the request.
                2. Use 3-6 sounds: at least one BED when the inventory has one, plus events that bring it to life.
                3. Volume range: 0.1 to 1.0. Beds that carry the scene are louder; events are usually softer.
                4. If the request is unrelated to sound, create a generic calm focus scene.
                5. Output STRICT JSON:
                {
                  "mix_name": "Name",
                  "description": "Short description",
                  "sounds": [ {"id": "sound_id", "volume": 0.5, "frequency": "NORMAL"} ]
                }
                "frequency" is only for EVENT sounds.
            """.trimIndent()

            val chatRequest = ChatCompletionRequest(
                model = ModelId(modelName),
                messages = listOf(
                    ChatMessage(role = ChatRole.System, content = systemInstruction),
                    ChatMessage(role = ChatRole.User, content = prompt)
                ),
                responseFormat = ChatResponseFormat.JsonObject
            )

            val response = openai.chatCompletion(chatRequest)

            val contentJson = response.choices.firstOrNull()?.message?.content
                ?: return Resource.Error(AppError.Ai.EmptyResponse())

            Resource.Success(json.decodeFromString<NetworkAiMix>(contentJson))
        } catch (e: OpenAIAPIException) {
            Resource.Error(
                AppError.Ai.ApiError(
                    code = e.statusCode ?: -1,
                    details = e.message ?: "Unknown AI Error"
                )
            )
        } catch (e: Exception) {
            Resource.Error(
                AppError.Ai.ApiError(
                    code = -1,
                    details = e.message ?: "Unknown AI Error"
                )
            )
        }
    }
}
