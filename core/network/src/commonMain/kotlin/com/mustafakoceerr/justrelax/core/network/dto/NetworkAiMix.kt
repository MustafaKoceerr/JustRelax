package com.mustafakoceerr.justrelax.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A sound the model may use, sent as JSON in the system prompt. */
@Serializable
data class NetworkAiSound(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("kind") val kind: String,
    @SerialName("tags") val tags: List<String>,
)

/** The JSON the model is asked to answer with. */
@Serializable
data class NetworkAiMix(
    @SerialName("mix_name") val mixName: String,
    @SerialName("description") val description: String,
    @SerialName("sounds") val sounds: List<NetworkAiMixSound>
)

/** [frequency] is RARE, NORMAL or OFTEN for an event sound. */
@Serializable
data class NetworkAiMixSound(
    @SerialName("id") val id: String,
    @SerialName("volume") val volume: Float,
    @SerialName("frequency") val frequency: String? = null,
)
