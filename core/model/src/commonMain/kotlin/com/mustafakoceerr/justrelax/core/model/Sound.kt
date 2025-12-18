package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Sound(
    val id: String,
    val name: String, // UI'da yerelleştirilecek
    val category: SoundCategory,
    val iconUrl: String,
    val audioUrl: String,
    val localPath: String?,
    val isDownloaded: Boolean = localPath != null
)