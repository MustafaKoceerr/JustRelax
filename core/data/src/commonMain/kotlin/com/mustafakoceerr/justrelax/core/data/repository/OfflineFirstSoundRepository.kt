package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.data.download.SoundDownloader
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.mapper.asSounds
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
    private val catalogSync: SoundCatalogSync,
    private val syncTimestamps: SyncTimestampStore,
    private val downloader: SoundDownloader,
    private val storage: SoundFileStorage,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
) : SoundRepository {

    override fun getSounds(): Flow<List<Sound>> =
        soundQueries.selectSoundsWithClips()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { it.asSounds(storage::resolveStoredPath) }

    override fun getSound(id: String): Flow<Sound?> =
        getSounds().map { sounds -> sounds.find { it.id == id } }

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
        val paths = downloader.download(sound) ?: return false
        saveClipPaths(sound.id, paths)
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
        emitAll(downloader.downloadAll(sounds, ::saveClipPathsOf))
    }

    private suspend fun saveClipPathsOf(sound: Sound, paths: List<String>) = saveClipPaths(sound.id, paths)

    private suspend fun saveClipPaths(soundId: String, paths: List<String>) {
        withContext(dispatchers.io) {
            soundQueries.transaction {
                paths.forEachIndexed { index, path ->
                    soundQueries.updateClipLocalPath(local_path = storage.toStoredPath(path), sound_id = soundId, idx = index.toLong())
                }
            }
        }
    }

    private companion object {
        val SYNC_INTERVAL = 24.hours
    }
}
