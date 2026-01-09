package com.mustafakoceerr.justrelax.core.domain.repository.sound

import com.mustafakoceerr.justrelax.core.common.Resource

interface SoundSyncRepository {
    suspend fun syncWithServer(): Resource<Unit>
}