package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.data.download.SoundDownloader
import com.mustafakoceerr.justrelax.core.data.mapper.DatabaseSoundMapper
import com.mustafakoceerr.justrelax.core.data.sync.SoundCatalogSync
import com.mustafakoceerr.justrelax.core.data.sync.SyncTimestampStore
import com.mustafakoceerr.justrelax.core.database.db.SoundQueries
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

internal class OfflineFirstSoundRepository(
    private val soundQueries: SoundQueries,
    private val soundMapper: DatabaseSoundMapper,
    private val catalogSync: SoundCatalogSync,
    private val syncTimestamps: SyncTimestampStore,
    private val downloader: SoundDownloader,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
) : SoundRepository {

    override fun getSounds(): Flow<List<Sound>> =
        soundQueries.selectAllSounds()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(soundMapper::toModelList)

    override fun getSound(id: String): Flow<Sound?> =
        soundQueries.selectSoundById(id)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.firstOrNull()?.let(soundMapper::toModel) }

    override suspend fun sync(): Resource<Unit> {
        val result = catalogSync.syncWithServer()
        if (result is Resource.Success) {
            syncTimestamps.setLastSoundSyncTimestamp(clock.now().toEpochMilliseconds())
        }
        return result
    }

    override suspend fun syncIfStale(): Resource<Unit> {
        val lastSync = syncTimestamps.getLastSoundSyncTimestamp().first()
        val age = clock.now().toEpochMilliseconds() - lastSync
        return if (age > SYNC_INTERVAL.inWholeMilliseconds) sync() else Resource.Success(Unit)
    }

    override suspend fun download(soundId: String): Boolean {
        val sound = getSound(soundId).first() ?: return false
        val path = downloader.download(sound.id, sound.remoteUrl) ?: return false
        updateLocalPath(sound.id, path)
        return true
    }

    override fun downloadStarterPack(): Flow<DownloadStatus> =
        syncThenDownload { it.isInitial && !it.isDownloaded }

    override fun downloadAll(): Flow<DownloadStatus> =
        syncThenDownload { !it.isDownloaded }

    private fun syncThenDownload(filter: (Sound) -> Boolean): Flow<DownloadStatus> = flow {
        val syncResult = sync()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message ?: "Sync failed"))
            return@flow
        }

        val sounds = getSounds().first().filter(filter)
        emitAll(downloader.downloadAll(sounds) { sound, path -> updateLocalPath(sound.id, path) })
    }

    private suspend fun updateLocalPath(soundId: String, localPath: String) {
        withContext(dispatchers.io) {
            soundQueries.updateLocalPath(localPath = localPath, id = soundId)
        }
    }

    private companion object {
        val SYNC_INTERVAL = 24.hours
    }
}
