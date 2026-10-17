package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundSyncRepository

class FakeSoundSyncRepository(
    var result: Resource<Unit> = Resource.Success(Unit)
) : SoundSyncRepository {

    var syncCount = 0
        private set

    override suspend fun syncWithServer(): Resource<Unit> {
        syncCount++
        return result
    }
}
