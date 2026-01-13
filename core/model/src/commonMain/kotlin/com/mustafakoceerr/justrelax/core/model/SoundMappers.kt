package com.mustafakoceerr.justrelax.core.model

fun Sound.toSoundUi(languageCode: String): SoundUi {
    val resolvedName = names[languageCode]
        ?: names["en"]
        ?: names.values.firstOrNull()
        ?: id

    return SoundUi(
        id = id,
        name = resolvedName,
        categoryId = categoryId,
        iconUrl = iconUrl,
        remoteUrl = remoteUrl,
        localPath = localPath,
        isInitial = isInitial,
        sizeBytes = sizeBytes,
        isDownloaded = isDownloaded
    )
}