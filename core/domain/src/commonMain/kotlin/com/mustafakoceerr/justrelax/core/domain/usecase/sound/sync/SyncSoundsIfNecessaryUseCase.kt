package com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundSyncRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SyncSoundsIfNecessaryUseCase(
    private val dataSourceStateRepository: DataSourceStateRepository,
    private val soundSyncRepository: SoundSyncRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(): Resource<Unit> {
        val lastSyncTimestamp = dataSourceStateRepository.getLastSoundSyncTimestamp().first()
        val now = clock.now().toEpochMilliseconds()

        if ((now - lastSyncTimestamp) > SYNC_INTERVAL.inWholeMilliseconds) {
            val syncResult = soundSyncRepository.syncWithServer()
            if (syncResult is Resource.Success) {
                dataSourceStateRepository.setLastSoundSyncTimestamp(now)
            }
            return syncResult
        }
        return Resource.Success(Unit)
    }

    private companion object {
        val SYNC_INTERVAL = 24.hours
    }
}
