package com.mustafakoceerr.justrelax.feature.ai.data.repository

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.feature.ai.BuildConfig
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiMixResponse
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import kotlinx.serialization.json.Json

class OpenAiRepositoryImpl : AiRepository {

    private val openai = OpenAI(token = BuildConfig.OPENAI_API_KEY)
    private val modelName = "gpt-4o-mini"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun generateMix(
        prompt: String,
        availableSoundIds: List<String>
    ): Resource<AiMixResponse> {
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

            val aiMixResponse = json.decodeFromString<AiMixResponse>(contentJson)
            Resource.Success(aiMixResponse)

        } catch (e: OpenAIAPIException) {
            val code = e.statusCode ?: -1
            Resource.Error(
                AppError.Ai.ApiError(
                    code = code,
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