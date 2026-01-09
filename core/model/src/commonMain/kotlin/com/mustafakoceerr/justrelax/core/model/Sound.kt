package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Sound(
    val id: String,
    val names: Map<String, String>,
    val categoryId: String,
    val iconUrl: String,
    val remoteUrl: String,
    val localPath: String? = null,
    val isInitial: Boolean,
    val sizeBytes: Long
) {
    val isDownloaded: Boolean
        get() = localPath != null

    fun getDisplayName(languageCode: String): String {
        return names[languageCode]
            ?: names["en"]
            ?: names.values.firstOrNull()
            ?: id
    }
}