package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound

/** The built-in sound catalog, with absolute clip URLs. */
interface SoundRemoteDataSource {
    suspend fun getSounds(): List<NetworkSound>
}
