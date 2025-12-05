package com.mustafakoceerr.justrelax.feature.ai.data.model

import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- BİZİM UYGULAMANIN KULLANACAĞI CEVAP MODELİ ---
@Serializable
data class AiMixResponse(
    @SerialName("mix_name") val mixName: String,
    @SerialName("description") val description: String,
    @SerialName("sounds") val sounds: List<AiMixSound>
)

@Serializable
data class AiMixSound(
    @SerialName("id") val id: String,
    @SerialName("volume") val volume: Float
)

// --- GEMINI API REQUEST/RESPONSE MODELLERİ ---
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String = "application/json" // JSON zorlaması
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)
