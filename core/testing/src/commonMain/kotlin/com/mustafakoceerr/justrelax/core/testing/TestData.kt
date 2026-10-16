package com.mustafakoceerr.justrelax.core.testing

import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind

/** A built-in bed with one clip; [localPath] marks it downloaded. */
fun testSound(
    id: String,
    localPath: String? = null,
    isInitial: Boolean = false,
    kind: SoundKind = SoundKind.BED,
    source: SoundSource = SoundSource.BUILT_IN,
    category: SoundCategory = SoundCategory.NATURE,
    defaultFrequency: EventFrequency = EventFrequency.NORMAL,
    clips: List<SoundClip> = listOf(testClip("https://cdn.test/$id.mp3", localPath)),
) = Sound(
    id = id,
    names = mapOf("en" to id),
    category = category,
    kind = kind,
    source = source,
    clips = clips,
    license = License.CC0,
    defaultFrequency = defaultFrequency,
    isInitial = isInitial,
)

fun testClip(remoteUrl: String?, localPath: String? = null, sizeBytes: Long = 1_000L) =
    SoundClip(remoteUrl = remoteUrl, localPath = localPath, durationMs = 2_000L, sizeBytes = sizeBytes)
