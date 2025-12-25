package com.mustafakoceerr.justrelax.feature.ai.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.feature.ai.BuildConfig
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiMixResponse
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- OPENAI DTOs (Private - Sadece bu sınıf bilir) ---
@Serializable
private data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat()
)

@Serializable
private data class OpenAiMessage(
    val role: String, // "system" veya "user"
    val content: String
)

@Serializable
private data class ResponseFormat(
    val type: String = "json_object" // JSON zorlaması için kritik
)

@Serializable
private data class OpenAiResponse(
    val choices: List<OpenAiChoice>
)

@Serializable
private data class OpenAiChoice(
    val message: OpenAiMessage
)

// --- IMPLEMENTATION ---
class OpenAiRepositoryImpl(
    private val client: HttpClient
) : AiRepository {

    private val apiKey = BuildConfig.OPENAI_API_KEY

    private val modelName = "gpt-4o-mini"
    private val endpoint = "https://api.openai.com/v1/chat/completions"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override suspend fun generateMix(
        prompt: String,
        availableSoundIds: List<String>
    ): Resource<AiMixResponse> {
        return try {
            // 1. Validasyon
            if (availableSoundIds.isEmpty()) {
                return Resource.Error(AppError.Ai.NoDownloadedSounds)
            }

            // 2. System Prompt (Kişilik ve Kurallar)
            val inventoryString = availableSoundIds.joinToString(", ")

            val systemInstruction = """
                You are an expert ambient sound DJ.
                
                ### INVENTORY (IDs):
                [$inventoryString]
                
                ### RULES:
                1. Create a mix using ONLY the provided IDs based on the user's request.
                2. If the user request is unrelated, create a generic "Focus Mix".
                3. Use 2-5 sounds. Volume range: 0.1 to 1.0.
                4. Output STRICT JSON format:
                {
                  "mix_name": "Name",
                  "description": "Short desc",
                  "sounds": [ {"id": "sound_id", "volume": 0.5} ]
                }
            """.trimIndent()

            // 3. İsteği Hazırla
            val requestBody = OpenAiRequest(
                model = modelName,
                messages = listOf(
                    OpenAiMessage(role = "system", content = systemInstruction),
                    OpenAiMessage(role = "user", content = prompt)
                )
            )

            // 4. API Çağrısı
            val response = client.post(endpoint) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseBody = response.bodyAsText()

            if (!response.status.isSuccess()) {
                val errorDetails = "${response.status} - $responseBody"
                println("OpenAI Error: $errorDetails")

                // DÜZELTME: Parametreyi içeri veriyoruz
                return Resource.Error(AppError.Ai.ApiError(errorDetails))
            }

            // 5. Response Parsing
            val openAiResponse = json.decodeFromString<OpenAiResponse>(responseBody)
            val contentJson = openAiResponse.choices.firstOrNull()?.message?.content
                ?: return Resource.Error(AppError.Ai.EmptyResponse)

            // 6. Domain Modeline Çevir
            val aiMixResponse = json.decodeFromString<AiMixResponse>(contentJson)
            Resource.Success(aiMixResponse)

        } catch (e: Exception) {
            e.printStackTrace()
            // JSON parse hatası veya ağ hatası
            Resource.Error(AppError.Unknown(e))
        }
    }
}