package com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundSyncRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SyncSoundsUseCase(
    private val soundSyncRepository: SoundSyncRepository,
    private val dataSourceStateRepository: DataSourceStateRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): Resource<Unit> {
        val result = soundSyncRepository.syncWithServer()
        if (result is Resource.Success) {
            dataSourceStateRepository.setLastSoundSyncTimestamp(
                Clock.System.now().toEpochMilliseconds()
            )
        }
        return result
    }
}