package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.model.Sound

interface SoundRemoteDataSource {
    suspend fun getSounds(): List<Sound>
}