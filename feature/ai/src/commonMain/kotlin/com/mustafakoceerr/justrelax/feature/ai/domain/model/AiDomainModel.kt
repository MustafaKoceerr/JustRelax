package com.mustafakoceerr.justrelax.feature.ai.domain.model

import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 1. MODELLER (Saf Veri) ---

/**
 * AI'dan dönen ve UI'ın kullanacağı nihai sonuç.
 */
// --- HAM VERİ (Repository'den gelen) ---
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

// --- ZENGİN VERİ (UseCase'den çıkan & UI'ın kullandığı) ---
data class AiGeneratedMix(
    val name: String,
    val description: String,
    // ID değil, gerçek Sound nesnesi tutar. Controller bunu sever.
    val sounds: Map<Sound, Float>
)