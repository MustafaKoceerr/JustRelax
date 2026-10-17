package com.mustafakoceerr.justrelax.core.model

fun Sound.localized(languageCode: String) = LocalizedSound(
    id = id,
    name = getDisplayName(languageCode),
    categoryId = categoryId,
    iconUrl = iconUrl,
    remoteUrl = remoteUrl,
    localPath = localPath,
    isInitial = isInitial,
    sizeBytes = sizeBytes,
    isDownloaded = isDownloaded,
)
