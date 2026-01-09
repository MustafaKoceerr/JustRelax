package com.mustafakoceerr.justrelax.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.database.db.SoundQueries
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SoundRepositoryImpl(
    private val soundQueries: SoundQueries,
    private val soundMapper: DatabaseSoundMapper,
    private val dispatchers: DispatcherProvider
) : SoundRepository {

    override fun getSounds(): Flow<List<Sound>> {
        return soundQueries.selectAllSounds()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { soundEntities ->
                soundMapper.toModelList(soundEntities)
            }
    }

    override fun getSound(id: String): Flow<Sound?> {
        return soundQueries.selectSoundById(id)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { soundEntities ->
                soundEntities.firstOrNull()?.let { soundMapper.toModel(it) }
            }
    }

    override suspend fun updateLocalPath(soundId: String, localPath: String?) {
        withContext(dispatchers.io) {
            soundQueries.updateLocalPath(localPath = localPath, id = soundId)
        }
    }
}