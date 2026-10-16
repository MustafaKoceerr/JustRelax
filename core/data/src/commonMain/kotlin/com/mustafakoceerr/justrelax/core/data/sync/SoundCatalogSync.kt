package com.mustafakoceerr.justrelax.core.data.sync

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.mapper.asSoundOrNull
import com.mustafakoceerr.justrelax.core.data.mapper.insert
import com.mustafakoceerr.justrelax.core.data.mapper.updateMetadata
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.database.db.Sound_clip
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/** Brings the local built-in sounds in line with the server catalog. */
internal fun interface SoundCatalogSync {
    suspend fun syncWithServer(): Resource<Unit>
}

/**
 * Inserts new catalog sounds, deletes removed ones and updates changed ones in place, together
 * with their clips. Only BUILT_IN rows are touched: Freesound and the user's own sounds are not
 * in the catalog. It never uses REPLACE, which would cascade-delete the sound from saved scenes.
 * Files of removed or changed clips are deleted after the transaction.
 */
internal class DatabaseSoundCatalogSync(
    private val remoteDataSource: SoundRemoteDataSource,
    private val database: JustRelaxDatabase,
    private val storage: SoundFileStorage,
    private val dispatchers: DispatcherProvider,
) : SoundCatalogSync {

    private val queries get() = database.soundQueries

    override suspend fun syncWithServer(): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val catalog = remoteDataSource.getSounds().mapNotNull { it.asSoundOrNull() }
            val obsoleteFiles = database.transactionWithResult { apply(catalog) }
            obsoleteFiles.forEach { deleteQuietly(it) }
            Resource.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError) {
            Resource.Error(e)
        } catch (e: Exception) {
            Resource.Error(AppError.Unknown(e))
        }
    }

    /** Returns the local files that no longer belong to any clip. */
    private fun apply(catalog: List<Sound>): List<String> {
        val obsoleteFiles = mutableListOf<String>()
        val localSources = queries.selectAllSounds().executeAsList().associate { it.id to it.source }
        val localClips = queries.selectAllClips().executeAsList().groupBy { it.sound_id }

        val catalogIds = catalog.mapTo(mutableSetOf()) { it.id }
        localSources.filter { (id, source) -> source == SoundSource.BUILT_IN && id !in catalogIds }.keys.forEach { id ->
            obsoleteFiles += localClips[id].orEmpty().mapNotNull { it.local_path }
            queries.deleteSoundById(id)
        }

        catalog.forEach { sound ->
            when (localSources[sound.id]) {
                null -> queries.insert(sound)
                SoundSource.BUILT_IN -> {
                    queries.updateMetadata(sound)
                    obsoleteFiles += syncClips(sound, localClips[sound.id].orEmpty())
                }
                // The id belongs to a sound from another source; the catalog cannot take it over.
                else -> Unit
            }
        }
        return obsoleteFiles
    }

    /** A clip whose file changed must be downloaded again; returns the files that were replaced. */
    private fun syncClips(sound: Sound, local: List<Sound_clip>): List<String> {
        val obsoleteFiles = mutableListOf<String>()
        sound.clips.forEachIndexed { index, clip ->
            val existing = local.getOrNull(index)
            when {
                existing == null ->
                    queries.insertClip(sound.id, index.toLong(), clip.remoteUrl, null, clip.durationMs, clip.sizeBytes)
                !existing.isSameFileAs(clip) -> {
                    existing.local_path?.let { obsoleteFiles += it }
                    queries.replaceClipFile(clip.remoteUrl, clip.durationMs, clip.sizeBytes, sound.id, index.toLong())
                }
            }
        }
        obsoleteFiles += local.drop(sound.clips.size).mapNotNull { it.local_path }
        queries.deleteClipsFrom(sound.id, sound.clips.size.toLong())
        return obsoleteFiles
    }

    private fun Sound_clip.isSameFileAs(clip: SoundClip) =
        remote_url == clip.remoteUrl && duration_ms == clip.durationMs && size_bytes == clip.sizeBytes

    private suspend fun deleteQuietly(path: String) {
        try {
            storage.deleteFile(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // An orphaned file only costs space; the sync itself succeeded.
        }
    }
}
