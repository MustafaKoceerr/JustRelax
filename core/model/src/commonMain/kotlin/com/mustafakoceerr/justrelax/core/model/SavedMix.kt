package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedMix(
    val id: Long,
    val name: String,
    val dateEpoch: Long,
    val sounds: List<SavedSound>
)

