package com.mustafakoceerr.justrelax.core.model

/** A [Sound] with its name resolved for the current app language. */
data class LocalizedSound(
    val id: String,
    val name: String,
    val categoryId: String,
    val iconUrl: String,
    val remoteUrl: String,
    val localPath: String?,
    val isInitial: Boolean,
    val sizeBytes: Long,
    val isDownloaded: Boolean
)