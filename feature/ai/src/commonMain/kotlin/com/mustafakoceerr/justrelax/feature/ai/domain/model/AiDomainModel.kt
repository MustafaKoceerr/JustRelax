package com.mustafakoceerr.justrelax.feature.ai.domain.model

import com.mustafakoceerr.justrelax.core.model.SoundUi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

data class AiGeneratedMix(
    val name: String,
    val description: String,
    val sounds: Map<SoundUi, Float>
)