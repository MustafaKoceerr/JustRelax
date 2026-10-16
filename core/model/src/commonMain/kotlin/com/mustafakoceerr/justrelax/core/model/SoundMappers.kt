package com.mustafakoceerr.justrelax.core.model

fun Sound.localized(languageCode: String) = LocalizedSound(
    id = id,
    name = getDisplayName(languageCode),
    category = category,
    icon = icon,
    kind = kind,
    source = source,
    sizeBytes = sizeBytes,
    isDownloaded = isDownloaded,
)
