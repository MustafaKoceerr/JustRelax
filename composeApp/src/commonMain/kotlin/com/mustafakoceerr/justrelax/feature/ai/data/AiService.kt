package com.mustafakoceerr.justrelax.feature.ai.data

import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.ai.data.model.AiMixResponse
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiContent
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiPart
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiRequest
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

interface AiService {
    suspend fun generateMix(userPrompt: String): Result<AiMixResponse>
}

class AiServiceImpl(
    private val client: HttpClient,
    private val soundRepository: SoundRepository
) : AiService {

    private val API_KEY = API_KEY_CONSTANTS

    // DÜZELTME: Listende açıkça görünen "gemini-2.0-flash" modelini kullanıyoruz.
    // Başındaki "models/" kısmını siliyoruz, sadece isim.
    private val MODEL_NAME = "gemini-2.0-flash"

    // URL yapısı
    private val FULL_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$API_KEY"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateMix(userPrompt: String): Result<AiMixResponse> {
        return try {
            val allSounds = soundRepository.getSounds().first()
            val soundInventory = allSounds.joinToString("\n") {
                "- ID: ${it.id} (Category: ${it.category.name})"
            }

            // PROMPT GÜNCELLENDİ: Örnek JSON formatı eklendi.
            val systemPrompt = """
                You are a professional ambient sound DJ.
                Your goal is to create a mix based on the user's mood or request.
                
                AVAILABLE SOUNDS (You can ONLY use these IDs):
                $soundInventory
                
                RULES:
                1. Select 2 to 5 sounds that match the user's request.
                2. Assign a volume (0.1 to 1.0) to each sound.
                3. Return ONLY valid JSON. No markdown, no extra text.
                4. You MUST include "mix_name" and "description" fields.
                
                REQUIRED JSON FORMAT EXAMPLE:
                {
                  "mix_name": "Forest Focus",
                  "description": "A calm mix with birds and wind to help you study.",
                  "sounds": [
                    { "id": "nature_1", "volume": 0.5 },
                    { "id": "air_1", "volume": 0.3 }
                  ]
                }
                
                USER REQUEST: "$userPrompt"
            """.trimIndent()
            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))))
            )

            val response: GeminiResponse = client.post(FULL_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI")

            // Markdown temizliği
            val cleanJson = rawJson.replace("```json", "").replace("```", "").trim()

            val mixResponse = json.decodeFromString<AiMixResponse>(cleanJson)
            Result.success(mixResponse)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}