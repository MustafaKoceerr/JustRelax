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

// --- DEĞİŞİKLİK: HttpClient bağımlılığı kaldırıldı ---
class OpenAiRepositoryImpl : AiRepository {

    // 1. OpenAI Client'ı kütüphaneden oluşturuluyor.
    // API anahtarı ve diğer konfigürasyonlar burada merkezi olarak yönetilir.
    private val openai = OpenAI(token = BuildConfig.OPENAI_API_KEY)

    private val modelName = "gpt-4o-mini"

    // 2. JSON parser'ı sadece AI'dan gelen içeriği domain modeline çevirmek için kullanacağız.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // --- ÖNCEKİ DTO'LARIN TAMAMI SİLİNDİ ---
    // OpenAiRequest, OpenAiMessage, OpenAiResponse gibi sınıflara artık gerek yok.
    // Kütüphane kendi modellerini sağlıyor.

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
                3. Use 2-7 sounds. Volume range: 0.1 to 1.0.
                4. Output STRICT JSON format:
                {
                  "mix_name": "Name",
                  "description": "Short desc",
                  "sounds": [ {"id": "sound_id", "volume": 0.5} ]
                }
            """.trimIndent()

            // 3. Kütüphanenin kendi modelleriyle istek oluşturuluyor.
            // Bu, DTO'ları manuel yönetmekten çok daha güvenli ve basittir.
            val chatRequest = ChatCompletionRequest(
                model = ModelId(modelName),
                messages = listOf(
                    ChatMessage(role = ChatRole.System, content = systemInstruction),
                    ChatMessage(role = ChatRole.User, content = prompt)
                ),
                // JSON formatında cevap vermeye zorluyoruz.
                responseFormat = ChatResponseFormat.JsonObject
            )

            // 4. API çağrısı tek bir fonksiyon ile yapılıyor.
            val response = openai.chatCompletion(chatRequest)

            // 5. Cevap doğrudan type-safe bir nesne olarak geliyor.
            val contentJson = response.choices.firstOrNull()?.message?.content
                ?: return Resource.Error(AppError.Ai.EmptyResponse())

            // 6. Gelen JSON string'ini kendi domain modelimize parse ediyoruz.
            val aiMixResponse = json.decodeFromString<AiMixResponse>(contentJson)
            Resource.Success(aiMixResponse)

        } catch (e: OpenAIAPIException) {
            val code = e.statusCode ?: -1   // veya e.code / e.httpStatus
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