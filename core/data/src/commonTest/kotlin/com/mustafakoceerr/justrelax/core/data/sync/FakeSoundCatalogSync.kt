package com.mustafakoceerr.justrelax.core.data.sync

import com.mustafakoceerr.justrelax.core.common.Resource

internal class FakeSoundCatalogSync(
    var result: Resource<Unit> = Resource.Success(Unit)
) : SoundCatalogSync {

    var syncCount = 0
        private set

    override suspend fun syncWithServer(): Resource<Unit> {
        syncCount++
        return result
    }
}
