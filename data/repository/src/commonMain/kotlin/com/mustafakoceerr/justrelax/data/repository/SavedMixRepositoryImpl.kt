package com.mustafakoceerr.justrelax.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.database.db.SavedMixQueries
import com.mustafakoceerr.justrelax.core.database.db.SoundQueries
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class SavedMixRepositoryImpl(
    private val savedMixQueries: SavedMixQueries,
    private val soundQueries: SoundQueries,
    private val soundMapper: DatabaseSoundMapper,
    private val dispatchers: DispatcherProvider
) : SavedMixRepository {

    override fun getSavedMixes(): Flow<List<SavedMix>> {
        return combine(
            savedMixQueries.selectAllMixes().asFlow().mapToList(dispatchers.io),
            savedMixQueries.selectAllRelations().asFlow().mapToList(dispatchers.io),
            soundQueries.selectAllSounds().asFlow().mapToList(dispatchers.io)
        ) { mixEntities, relationEntities, soundEntities ->

            val allSoundsMap = soundMapper.toModelList(soundEntities).associateBy { it.id }
            val relationsByMixId = relationEntities.groupBy { it.mix_id }

            mixEntities.map { mixEntity ->
                val relations = relationsByMixId[mixEntity.id] ?: emptyList()

                val soundsMap = relations.mapNotNull { relation ->
                    val sound = allSoundsMap[relation.sound_id]
                    if (sound != null) {
                        sound to relation.volume.toFloat()
                    } else {
                        null
                    }
                }.toMap()

                SavedMix(
                    id = mixEntity.id,
                    name = mixEntity.name,
                    createdAt = mixEntity.created_at,
                    sounds = soundsMap
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun saveMix(name: String, soundVolumes: Map<String, Float>) {
        withContext(dispatchers.io) {
            savedMixQueries.transaction {
                val currentTime = Clock.System.now().toString()
                savedMixQueries.insertMix(name, currentTime)
                val mixId = savedMixQueries.lastInsertRowId().executeAsOne()

                soundVolumes.forEach { (soundId, volume) ->
                    savedMixQueries.insertMixSound(
                        mix_id = mixId,
                        sound_id = soundId,
                        volume = volume.toDouble()
                    )
                }
            }
        }
    }

    override suspend fun deleteMix(id: Long) {
        withContext(dispatchers.io) {
            savedMixQueries.deleteMixById(id)
        }
    }
}