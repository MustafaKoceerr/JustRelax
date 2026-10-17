package com.mustafakoceerr.justrelax.core.model

/** A search result from Freesound with a license the app may use. */
data class FreesoundSound(
    val id: Long,
    val name: String,
    val author: String,
    val license: License,
    val pageUrl: String,
    val tags: List<String>,
    val durationMs: Long,
    /** A high-quality preview; it needs no login to download, unlike the original file. */
    val previewUrl: String,
)

/** Where a sound added on the device came from, when it is not the user's own recording. */
data class SoundOrigin(
    val remoteId: String,
    val license: License,
    val attribution: Attribution,
    val tags: List<String>,
)
