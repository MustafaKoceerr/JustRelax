package com.mustafakoceerr.justrelax.core.data.sync

import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.model.Sound

internal class FakeSoundRemoteDataSource(var sounds: List<Sound> = emptyList()) : SoundRemoteDataSource {
    var error: Exception? = null

    override suspend fun getSounds(): List<Sound> {
        error?.let { throw it }
        return sounds
    }
}
