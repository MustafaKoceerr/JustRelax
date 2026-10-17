package com.mustafakoceerr.justrelax.core.model

import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind

/** A [Sound] with its name resolved for the current app language. */
data class LocalizedSound(
    val id: String,
    val name: String,
    val category: SoundCategory,
    val icon: SoundIcon,
    val kind: SoundKind,
    val source: SoundSource,
    val sizeBytes: Long,
    val isDownloaded: Boolean,
)
