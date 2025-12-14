package com.mustafakoceerr.justrelax.feature.ai.data.repository

import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.ai.BuildConfig
import com.mustafakoceerr.justrelax.feature.ai.data.model.AiMixResponse
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiContent
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiPart
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiRequest
import com.mustafakoceerr.justrelax.feature.ai.data.model.GeminiResponse
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiService
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class AiServiceImpl(
    private val client: HttpClient,
    private val soundRepository: SoundRepository
) : AiService {

    private val API_KEY = BuildConfig.GEMINI_API_KEY
//    private val MODEL_NAME = "gemini-2.5-flash-lite"
    private val MODEL_NAME = "gemma-3-27b-it"
    private val FULL_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$API_KEY"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true // Gevşek JSON parsing (AI bazen hata yapabilir)
    }

    override suspend fun generateMix(userPrompt: String): Result<AiMixResponse> {
        return try {
            println("AI_DEBUG: === AI İSTEĞİ BAŞLIYOR ===")
            println("AI_DEBUG: Kullanılan Model: $MODEL_NAME")

            val downloadedSounds = soundRepository.getDownloadedSounds().first()

            if (downloadedSounds.isEmpty()) {
                println("AI_DEBUG: HATA - İndirilmiş ses yok.")
                throw IllegalStateException("No downloaded sounds available.")
            }

            val soundInventory = downloadedSounds.joinToString("\n") {
                "- ID: ${it.id} (Category: ${it.category.name})"
            }

            println("AI_DEBUG: Envanterde ${downloadedSounds.size} ses var.")

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

            // 1. İsteği gönder ama hemen objeye çevirme (bodyAsText kullan)
            val httpResponse = client.post(FULL_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseStatus = httpResponse.status
            val rawResponseBody = httpResponse.bodyAsText() // <--- EN ÖNEMLİ KISIM

            println("AI_DEBUG: HTTP Status Code: $responseStatus")
            println("AI_DEBUG: Raw Response Body: $rawResponseBody")

            // 2. HTTP Hatası var mı?
            if (!responseStatus.isSuccess()) {
                throw Exception("API Error ($responseStatus): $rawResponseBody")
            }

            // 3. JSON Parse İşlemi (GeminiResponse'a çevir)
            val geminiResponse = try {
                json.decodeFromString<GeminiResponse>(rawResponseBody)
            } catch (e: Exception) {
                println("AI_DEBUG: GeminiResponse Parse Hatası: ${e.message}")
                throw e
            }

            val candidate = geminiResponse.candidates?.firstOrNull()

            // Güvenlik veya başka bir sebeple içerik boş mu?
            if (candidate?.content == null) {
                println("AI_DEBUG: İçerik BOŞ! FinishReason: ${candidate}")
                throw Exception("AI returned no content. Reason: ${candidate}")
            }

            val rawJsonText = candidate.content.parts.firstOrNull()?.text
                ?: throw Exception("Empty text in AI response")

            // Markdown temizliği
            val cleanJson = rawJsonText.replace("```json", "").replace("```", "").trim()

            println("AI_DEBUG: Temizlenmiş JSON: $cleanJson")

            // 4. Bizim modelimize çevir (AiMixResponse)
            val mixResponse = json.decodeFromString<AiMixResponse>(cleanJson)

            println("AI_DEBUG: === İŞLEM BAŞARILI ===")
            Result.success(mixResponse)

        } catch (e: Exception) {
            println("AI_DEBUG: !!! KRİTİK HATA !!!")
            println("AI_DEBUG: Hata Mesajı: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}