package com.mustafakoceerr.justrelax.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkSound(
    val id: String,
    val names: Map<String, String>,
    val category: String,
    @SerialName("icon_url") val iconUrl: String,
    @SerialName("audio_url") val audioUrl: String,
    val version: Int,
    @SerialName("is_initial") val isInitial: Boolean = false,
    @SerialName("size_bytes") val sizeBytes: Long = 0
)