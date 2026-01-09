package com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundSyncRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class SyncSoundsIfNecessaryUseCase(
    private val dataSourceStateRepository: DataSourceStateRepository,
    private val soundSyncRepository: SoundSyncRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): Resource<Unit> {
        val lastSyncTimestamp = dataSourceStateRepository.getLastSoundSyncTimestamp().first()
        val now = Clock.System.now().toEpochMilliseconds()
        val twentyFourHoursInMillis = 24.hours.inWholeMilliseconds

        if ((now - lastSyncTimestamp) > twentyFourHoursInMillis) {
            val syncResult = soundSyncRepository.syncWithServer()
            if (syncResult is Resource.Success) {
                dataSourceStateRepository.setLastSoundSyncTimestamp(now)
            }
            return syncResult
        }
        return Resource.Success(Unit)
    }
}