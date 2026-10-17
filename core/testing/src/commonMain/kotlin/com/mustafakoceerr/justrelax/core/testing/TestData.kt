package com.mustafakoceerr.justrelax.core.testing

import com.mustafakoceerr.justrelax.core.model.Sound

fun testSound(
    id: String,
    localPath: String? = null,
    isInitial: Boolean = false,
    remoteUrl: String = "https://cdn.test/$id.mp3",
    categoryId: String = "NATURE",
) = Sound(
    id = id,
    names = mapOf("en" to id),
    categoryId = categoryId,
    iconUrl = "https://cdn.test/$id.svg",
    remoteUrl = remoteUrl,
    localPath = localPath,
    isInitial = isInitial,
    sizeBytes = 1_000L,
)
