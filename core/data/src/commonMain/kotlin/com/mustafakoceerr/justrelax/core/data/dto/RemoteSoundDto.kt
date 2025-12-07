package com.mustafakoceerr.justrelax.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteSoundDto(
    @SerialName("id") val id: String,
    @SerialName("category") val category: String,
    @SerialName("names") val names: Map<String, String>,
    @SerialName("icon_url") val iconUrl: String,
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("version") val version: Int = 1
)