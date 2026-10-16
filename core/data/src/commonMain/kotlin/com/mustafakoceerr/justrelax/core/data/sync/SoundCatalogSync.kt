package com.mustafakoceerr.justrelax.core.data.sync

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.data.mapper.DatabaseSoundMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/** Brings the local sound table in line with the server catalog. */
internal fun interface SoundCatalogSync {
    suspend fun syncWithServer(): Resource<Unit>
}

/**
 * Inserts new sounds, deletes removed ones and updates changed metadata in place. It never uses
 * REPLACE: that would cascade-delete the sound from saved mixes and lose its local path.
 */
internal class DatabaseSoundCatalogSync(
    private val remoteDataSource: SoundRemoteDataSource,
    private val database: JustRelaxDatabase,
    private val soundMapper: DatabaseSoundMapper,
    private val dispatchers: DispatcherProvider
) : SoundCatalogSync {

    override suspend fun syncWithServer(): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val remoteSounds = remoteDataSource.getSounds()
            val localDbSounds = database.soundQueries.selectAllSounds().executeAsList()

            val remoteSoundsMap = remoteSounds.associateBy { it.id }
            val localSoundsMap = localDbSounds.associateBy { it.id }

            database.transaction {
                val soundsToDelete = localSoundsMap.keys - remoteSoundsMap.keys
                soundsToDelete.forEach { soundId ->
                    database.soundQueries.deleteSoundById(soundId)
                }

                remoteSounds.forEach { remoteSound ->
                    val localDbSound = localSoundsMap[remoteSound.id]

                    if (localDbSound == null) {
                        database.soundQueries.insertSound(
                            id = remoteSound.id,
                            names = remoteSound.names,
                            categoryId = remoteSound.categoryId,
                            iconUrl = remoteSound.iconUrl,
                            remoteUrl = remoteSound.remoteUrl,
                            localPath = null,
                            isInitial = remoteSound.isInitial,
                            sizeBytes = remoteSound.sizeBytes
                        )
                    } else {
                        // Remote sounds never carry a localPath, so compare without it.
                        val localModelSound = soundMapper.toModel(localDbSound).copy(localPath = null)
                        if (localModelSound != remoteSound.copy(localPath = null)) {
                            database.soundQueries.updateSoundMetadata(
                                id = remoteSound.id,
                                names = remoteSound.names,
                                categoryId = remoteSound.categoryId,
                                iconUrl = remoteSound.iconUrl,
                                remoteUrl = remoteSound.remoteUrl,
                                isInitial = remoteSound.isInitial,
                                sizeBytes = remoteSound.sizeBytes
                            )
                        }
                    }
                }
            }
            Resource.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError) {
            Resource.Error(e)
        } catch (e: Exception) {
            Resource.Error(AppError.Unknown(e))
        }
    }
}