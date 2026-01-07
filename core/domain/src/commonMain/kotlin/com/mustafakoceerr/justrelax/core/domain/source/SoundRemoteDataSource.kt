package com.mustafakoceerr.justrelax.core.domain.source

import com.mustafakoceerr.justrelax.core.model.Sound

interface SoundRemoteDataSource {
    suspend fun getSounds(): List<Sound>
}