package com.mustafakoceerr.justrelax.core.network.mapper

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound

/**
 * Sorumluluk: Network katmanından gelen DTO'ları Domain/Model katmanına çevirmek.
 * 'internal' olması, bu mapper'ın sadece bu modülün bir iç detayı olduğunu belirtir.
 */
internal class SoundMapper {

    /**
     * Tek bir NetworkSound nesnesini Sound modeline dönüştürür.
     */
    fun toModel(networkSound: NetworkSound): Sound {
        return Sound(
            id = networkSound.id,
            names = networkSound.names,
            categoryId = SoundCategory.fromId(networkSound.category).id,
            iconUrl = networkSound.iconUrl,
            remoteUrl = networkSound.audioUrl,
            localPath = null
        )
    }

    /**
     * Bir DTO listesini, bir Model listesine dönüştürür.
     */
    fun toModelList(networkSounds: List<NetworkSound>): List<Sound> {
        return networkSounds.map(::toModel)
    }
}