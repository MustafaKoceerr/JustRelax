package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.model.Sound

class FakeSoundRemoteDataSource(var sounds: List<Sound> = emptyList()) : SoundRemoteDataSource {
    var error: Exception? = null

    override suspend fun getSounds(): List<Sound> {
        error?.let { throw it }
        return sounds
    }
}
