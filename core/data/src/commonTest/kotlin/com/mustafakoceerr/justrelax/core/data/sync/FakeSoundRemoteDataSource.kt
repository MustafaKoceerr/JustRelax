package com.mustafakoceerr.justrelax.core.data.sync

import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundClip
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource

internal class FakeSoundRemoteDataSource(var sounds: List<NetworkSound> = emptyList()) : SoundRemoteDataSource {
    var error: Exception? = null

    override suspend fun getSounds(): List<NetworkSound> {
        error?.let { throw it }
        return sounds
    }
}

/** A CC0 catalog bed, or an event when it has more than one clip. */
internal fun networkSound(
    id: String,
    vararg clipUrls: String = arrayOf("https://cdn.test/$id.ogg"),
) = NetworkSound(
    id = id,
    names = mapOf("en" to id),
    category = "NATURE",
    kind = if (clipUrls.size > 1) "EVENT" else "BED",
    license = "CC0",
    clips = clipUrls.map { NetworkSoundClip(it, durationMs = 2_000, sizeBytes = 1_000) },
)
