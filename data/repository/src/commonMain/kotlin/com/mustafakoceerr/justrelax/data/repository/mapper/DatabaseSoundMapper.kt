package com.mustafakoceerr.justrelax.data.repository.mapper

import com.mustafakoceerr.justrelax.core.database.db.Sound as DbSound
import com.mustafakoceerr.justrelax.core.model.Sound as ModelSound

internal class DatabaseSoundMapper {

    fun toModel(dbSound: DbSound): ModelSound {
        return ModelSound(
            id = dbSound.id,
            names = dbSound.names,
            categoryId = dbSound.categoryId,
            iconUrl = dbSound.iconUrl,
            remoteUrl = dbSound.remoteUrl,
            localPath = dbSound.localPath,
            isInitial = dbSound.isInitial,
            sizeBytes = dbSound.sizeBytes
        )
    }

    fun toModelList(dbSounds: List<DbSound>): List<ModelSound> {
        return dbSounds.map(::toModel)
    }
}