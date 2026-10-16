package com.mustafakoceerr.justrelax.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The JSON the model is asked to answer with. */
@Serializable
data class NetworkAiMix(
    @SerialName("mix_name") val mixName: String,
    @SerialName("description") val description: String,
    @SerialName("sounds") val sounds: List<NetworkAiMixSound>
)

@Serializable
data class NetworkAiMixSound(
    @SerialName("id") val id: String,
    @SerialName("volume") val volume: Float
)
