package com.mustafakoceerr.justrelax.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.database.db.SoundQueries
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 'SoundRepository' arayüzünün SQLDelight kullanan implementasyonu.
 *
 * @param soundQueries Sadece 'sound' tablosuyla ilgili sorguları barındırır.
 * @param soundMapper  Veritabanı entity'lerini domain modeline çevirir.
 * @param dispatchers  I/O işlemlerinin hangi thread'de yapılacağını belirler.
 */
internal class SoundRepositoryImpl(
    private val soundQueries: SoundQueries,
    private val soundMapper: DatabaseSoundMapper, // YENİ: Mapper'ı enjekte alıyoruz.
    private val dispatchers: DispatcherProvider
) : SoundRepository {

    override fun getSounds(): Flow<List<Sound>> {
        return soundQueries.selectAllSounds()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { soundEntities ->
                // Dönüşüm işini artık Mapper yapıyor.
                soundMapper.toModelList(soundEntities)
            }
    }

    override fun getSound(id: String): Flow<com.mustafakoceerr.justrelax.core.model.Sound?> {
        return soundQueries.selectSoundById(id)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { soundEntities ->
                soundEntities.firstOrNull()?.let { soundMapper.toModel(it) }
            }
    }

    /**
     * YENİ: Belirli bir sesin 'localPath' alanını veritabanında günceller.
     * I/O işlemi olduğu için 'io' dispatcher'ında çalışır.
     */
    override suspend fun updateLocalPath(soundId: String, localPath: String?) {
        withContext(dispatchers.io) {
            soundQueries.updateLocalPath(localPath = localPath, id = soundId)
        }
    }
}