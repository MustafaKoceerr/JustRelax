package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedSound(
    val id: String,
    val volume: Float // 0.0f - 1.0f arası
)