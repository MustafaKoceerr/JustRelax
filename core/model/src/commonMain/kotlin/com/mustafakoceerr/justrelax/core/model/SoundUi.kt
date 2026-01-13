package com.mustafakoceerr.justrelax.core.model

data class SoundUi(
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