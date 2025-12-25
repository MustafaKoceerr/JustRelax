package com.mustafakoceerr.justrelax.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundSyncRepository
import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import kotlinx.coroutines.withContext

internal class SoundSyncRepositoryImpl(
    private val remoteDataSource: SoundRemoteDataSource,
    private val database: JustRelaxDatabase,
    private val soundMapper: DatabaseSoundMapper,
    private val dispatchers: DispatcherProvider
) : SoundSyncRepository {

    override suspend fun syncWithServer(): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val remoteSounds = remoteDataSource.getSounds()
            val localDbSounds = database.soundQueries.selectAllSounds().executeAsList()

            val remoteSoundsMap = remoteSounds.associateBy { it.id }
            val localSoundsMap = localDbSounds.associateBy { it.id }

            database.transaction {
                // Silme mantığı aynı
                val soundsToDelete = localSoundsMap.keys - remoteSoundsMap.keys
                soundsToDelete.forEach { soundId ->
                    database.soundQueries.deleteSoundById(soundId)
                }

                remoteSounds.forEach { remoteSound ->
                    val localDbSound = localSoundsMap[remoteSound.id]

                    // DEĞİŞİKLİK: 'insertOrReplace' sorgusuna yeni alanları ekliyoruz
                    if (localDbSound == null) {
                        database.soundQueries.insertOrReplace(
                            id = remoteSound.id,
                            names = remoteSound.names, // 'names' Map'ini veriyoruz
                            categoryId = remoteSound.categoryId,
                            iconUrl = remoteSound.iconUrl,
                            remoteUrl = remoteSound.remoteUrl,
                            localPath = null,
                            isInitial = remoteSound.isInitial, // 'isInitial' flag'ini veriyoruz
                            sizeBytes = remoteSound.sizeBytes

                        )
                    } else {
                        val localModelSound = soundMapper.toModel(localDbSound)
                        // Karşılaştırma için remoteSound'un da 'isInitial' alanını içermesi lazım
                        if (localModelSound != remoteSound) {
                            database.soundQueries.insertOrReplace(
                                id = remoteSound.id,
                                names = remoteSound.names,
                                categoryId = remoteSound.categoryId,
                                iconUrl = remoteSound.iconUrl,
                                remoteUrl = remoteSound.remoteUrl,
                                localPath = localDbSound.localPath, // Path'i koru
                                isInitial = remoteSound.isInitial,
                                sizeBytes = remoteSound.sizeBytes
                            )
                        }
                    }
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.Unknown(e))
        }
    }
}