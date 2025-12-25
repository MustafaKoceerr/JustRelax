package com.mustafakoceerr.justrelax.data.repository.mapper

// İsim çakışmasını önlemek için veritabanı entity'sine 'DbSound' alias'ı veriyoruz.
// Domain modelini de import ediyoruz.
import com.mustafakoceerr.justrelax.core.database.db.Sound as DbSound
import com.mustafakoceerr.justrelax.core.model.Sound as ModelSound

/**
 * Sorumluluk: Veritabanı katmanından gelen Entity'leri Domain/Model katmanına çevirmek.
 * 'internal' olması, bu mapper'ın sadece bu modülün bir iç detayı olduğunu belirtir.
 */
 class DatabaseSoundMapper {

    /**
     * Tek bir veritabanı Sound nesnesini (DbSound) domain Sound modeline (ModelSound) dönüştürür.
     */
    fun toModel(dbSound: DbSound): ModelSound {
        return ModelSound(
            id = dbSound.id,
            // DEĞİŞİKLİK: 'names' artık bir Map
            names = dbSound.names,
            categoryId = dbSound.categoryId,
            iconUrl = dbSound.iconUrl,
            remoteUrl = dbSound.remoteUrl,
            localPath = dbSound.localPath,
            isInitial = dbSound.isInitial,
            sizeBytes = dbSound.sizeBytes
        )
    }

    /**
     * Bir veritabanı entity listesini, bir domain model listesine dönüştürür.
     */
    fun toModelList(dbSounds: List<DbSound>): List<ModelSound> {
        return dbSounds.map(::toModel)
    }
}