package com.mustafakoceerr.justrelax.core.network.mapper

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound

internal class NetworkSoundToDomainMapper {

    fun toModel(dto: NetworkSound): Sound {
        return Sound(
            id = dto.id,
            names = dto.names,
            categoryId = dto.category,
            iconUrl = dto.iconUrl,
            remoteUrl = dto.audioUrl,
            localPath = null,
            isInitial = dto.isInitial,
            sizeBytes = dto.sizeBytes
        )
    }

    fun toModelList(dtos: List<NetworkSound>): List<Sound> {
        return dtos.map { toModel(it) }
    }
}