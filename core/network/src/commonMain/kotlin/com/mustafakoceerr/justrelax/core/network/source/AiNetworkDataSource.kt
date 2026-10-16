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
import kotlinx.serialization.json.Json

/** Asks a language model for a mix made only of the given sound ids. */
interface AiNetworkDataSource {
    suspend fun generateMix(prompt: String, availableSoundIds: List<String>): Resource<NetworkAiMix>
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
        availableSoundIds: List<String>
    ): Resource<NetworkAiMix> {
        return try {
            if (availableSoundIds.isEmpty()) {
                return Resource.Error(AppError.Ai.NoDownloadedSounds())
            }

            val inventoryString = availableSoundIds.joinToString(", ")
            val systemInstruction = """
                You are an expert ambient sound DJ.

                ### INVENTORY (IDs):
                [$inventoryString]

                ### RULES:
                1. Create a mix using ONLY the provided IDs based on the user's request.
                2. If the user request is unrelated, create a generic "Focus Mix".
                3. Use 3-7 sounds. Volume range: 0.1 to 1.0.
                4. Output STRICT JSON format:
                {
                  "mix_name": "Name",
                  "description": "Short desc",
                  "sounds": [ {"id": "sound_id", "volume": 0.5} ]
                }
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
