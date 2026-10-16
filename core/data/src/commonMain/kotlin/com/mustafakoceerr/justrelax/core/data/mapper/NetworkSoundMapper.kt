package com.mustafakoceerr.justrelax.core.data.mapper

import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound

/**
 * A catalog entry as a built-in sound, or null if the app cannot use it: an unknown kind or
 * license, no clips, or a CC-BY sound without the attribution its license requires.
 */
internal fun NetworkSound.asSoundOrNull(): Sound? {
    val kind = enumValueOrNull<SoundKind>(kind) ?: return null
    val license = enumValueOrNull<License>(license) ?: return null
    if (clips.isEmpty()) return null
    if (license.requiresAttribution && attribution == null) return null

    val category = SoundCategory.fromId(category)
    return Sound(
        id = id,
        names = names,
        category = category,
        kind = kind,
        source = SoundSource.BUILT_IN,
        clips = clips.map { SoundClip(remoteUrl = it.url, localPath = null, durationMs = it.durationMs, sizeBytes = it.sizeBytes) },
        license = license,
        icon = icon?.let { enumValueOrNull<SoundIcon>(it) } ?: category.defaultIcon,
        tags = tags,
        defaultFrequency = defaultFrequency?.let { enumValueOrNull<EventFrequency>(it) } ?: EventFrequency.NORMAL,
        attribution = attribution?.let { Attribution(title = it.title, author = it.author, sourceUrl = it.url) },
        isInitial = isInitial,
    )
}

private inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? =
    enumValues<T>().firstOrNull { it.name == name }
