package com.mustafakoceerr.justrelax.core.database.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.database.domain.model.SavedMix
import com.mustafakoceerr.justrelax.core.database.domain.model.SavedSound
import com.mustafakoceerr.justrelax.core.database.domain.repository.SavedMixRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SavedMixRepositoryImpl(
    private val db: JustRelaxDb
) : SavedMixRepository {

    private val queries = db.justRelaxDbQueries

    override fun getAllMixes(): Flow<List<SavedMix>> {
        return queries.getAllMixes()
            .asFlow()
            .mapToList(Dispatchers.IO) // List<MixEntity> olarak gelir
            .map { mixEntities ->
                // Her bir MixEntity için içindeki sesleri de çekip birleştiriyoruz (Mapping)
                mixEntities.map { mixEntity ->
                    // Bu mix'e ait sesleri çek
                    // Not: executeAsList() bloklayan bir işlemdir ama IO dispatcher içindeyiz, sorun yok.
                    val soundEntities = queries.getSoundsForMix(mixEntity.id).executeAsList()

                    val savedSounds = soundEntities.map { soundEntity ->
                        SavedSound(
                            id = soundEntity.soundId,
                            volume = soundEntity.volume.toFloat() // DB'den Double gelebilir, Float'a çevir
                        )
                    }

                    SavedMix(
                        id = mixEntity.id,
                        name = mixEntity.name,
                        dateEpoch = mixEntity.createdAt,
                        sounds = savedSounds
                    )
                }
            }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun saveMix(name: String, sounds: Map<String, Float>) {
        withContext(Dispatchers.IO) {
            // Transaction: Ya hepsi kaydedilir ya hiçbiri.
            queries.transaction {
                // 1. Mix'i kaydet (Tarih şu an)
                val now = Clock.System.now().toEpochMilliseconds()
                queries.insertMix(name, now)

                // 2. Oluşan Mix'in ID'sini al
                val mixId = queries.selectLastInsertId().executeAsOne()

                // 3. Sesleri tek tek kaydet
                sounds.forEach { (soundId, volume) ->
                    queries.insertMixSound(mixId, soundId, volume.toDouble())
                }
            }
        }
    }

    override suspend fun deleteMix(id: Long) {
        withContext(Dispatchers.IO) {
            // Cascade açık olduğu için sadece Mix'i silmek yeterli, sesler otomatik silinir.
            queries.deleteMixById(id)
        }
    }

    override suspend fun isMixNameExists(name: String): Boolean {
        return withContext(Dispatchers.IO){
            // executeAsOne() Long döner. 0'dan büyükse true.
            val count = queries.countMixesByName(name).executeAsOne()
            count > 0
        }
    }
}