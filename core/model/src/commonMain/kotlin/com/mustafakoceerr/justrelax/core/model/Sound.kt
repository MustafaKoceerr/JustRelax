package com.mustafakoceerr.justrelax.core.model

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind

/**
 * A sound in the library: one looping clip for a [SoundKind.BED], one or more variations for a
 * [SoundKind.EVENT]. Built-in sounds come from the catalog; Freesound and the user's own sounds
 * are added on the device and never touched by a catalog sync.
 */
data class Sound(
    val id: String,
    val names: Map<String, String>,
    val category: SoundCategory,
    val kind: SoundKind,
    val source: SoundSource,
    val clips: List<SoundClip>,
    val license: License,
    val icon: SoundIcon = category.defaultIcon,
    val tags: List<String> = emptyList(),
    /** How often an event plays when first added to a scene; beds ignore it. */
    val defaultFrequency: EventFrequency = EventFrequency.NORMAL,
    val attribution: Attribution? = null,
    /** The sound's id at its source, e.g. the Freesound id. */
    val remoteId: String? = null,
    /** Downloaded on first launch. */
    val isInitial: Boolean = false,
) {
    val isDownloaded: Boolean
        get() = clips.isNotEmpty() && clips.all { it.isDownloaded }

    val sizeBytes: Long
        get() = clips.sumOf { it.sizeBytes }

    fun getDisplayName(languageCode: String): String {
        return names[languageCode]
            ?: names["en"]
            ?: names.values.firstOrNull()
            ?: id
    }
}

/** One audio file of a sound. [remoteUrl] is null for files that only exist on the device. */
data class SoundClip(
    val remoteUrl: String?,
    val localPath: String?,
    val durationMs: Long,
    val sizeBytes: Long,
) {
    val isDownloaded: Boolean get() = localPath != null
}

enum class SoundSource { BUILT_IN, FREESOUND, USER }
