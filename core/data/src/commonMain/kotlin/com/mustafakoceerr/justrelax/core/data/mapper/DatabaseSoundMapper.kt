package com.mustafakoceerr.justrelax.core.data.mapper

import com.mustafakoceerr.justrelax.core.database.db.SelectSoundsWithClips
import com.mustafakoceerr.justrelax.core.database.db.SoundQueries
import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundClip

/**
 * Rows of `selectSoundsWithClips` (one per clip, ordered) as sounds, in library order. Stored clip
 * paths go through [resolvePath] (SoundFileStorage.resolveStoredPath) to become files on the device.
 */
internal fun List<SelectSoundsWithClips>.asSounds(resolvePath: (String) -> String = { it }): List<Sound> =
    groupBy { it.id }.values.map { rows ->
        val sound = rows.first()
        Sound(
            id = sound.id,
            names = sound.names,
            category = sound.category,
            kind = sound.kind,
            source = sound.source,
            // A sound without clips has one row with null clip columns.
            clips = rows.mapNotNull { row ->
                SoundClip(
                    remoteUrl = row.remote_url,
                    localPath = row.local_path?.let(resolvePath),
                    durationMs = row.duration_ms ?: return@mapNotNull null,
                    sizeBytes = row.size_bytes ?: return@mapNotNull null,
                )
            },
            license = sound.license,
            icon = sound.icon,
            tags = sound.tags,
            defaultFrequency = sound.default_frequency,
            attribution = sound.attribution_title?.let { title ->
                Attribution(title = title, author = sound.attribution_author.orEmpty(), sourceUrl = sound.attribution_url.orEmpty())
            },
            remoteId = sound.remote_id,
            isInitial = sound.is_initial,
        )
    }

/** Inserts a sound and its clips; call inside a transaction. [storePath] is SoundFileStorage.toStoredPath. */
internal fun SoundQueries.insert(sound: Sound, storePath: (String) -> String = { it }) {
    insertSound(
        id = sound.id,
        source = sound.source,
        remote_id = sound.remoteId,
        names = sound.names,
        category = sound.category,
        icon = sound.icon,
        kind = sound.kind,
        tags = sound.tags,
        default_frequency = sound.defaultFrequency,
        license = sound.license,
        attribution_title = sound.attribution?.title,
        attribution_author = sound.attribution?.author,
        attribution_url = sound.attribution?.sourceUrl,
        is_initial = sound.isInitial,
    )
    sound.clips.forEachIndexed { index, clip ->
        insertClip(sound.id, index.toLong(), clip.remoteUrl, clip.localPath?.let(storePath), clip.durationMs, clip.sizeBytes)
    }
}

/** Updates what the catalog owns; the clips are synced separately. */
internal fun SoundQueries.updateMetadata(sound: Sound) {
    updateSoundMetadata(
        names = sound.names,
        category = sound.category,
        icon = sound.icon,
        kind = sound.kind,
        tags = sound.tags,
        default_frequency = sound.defaultFrequency,
        license = sound.license,
        attribution_title = sound.attribution?.title,
        attribution_author = sound.attribution?.author,
        attribution_url = sound.attribution?.sourceUrl,
        is_initial = sound.isInitial,
        id = sound.id,
    )
}
