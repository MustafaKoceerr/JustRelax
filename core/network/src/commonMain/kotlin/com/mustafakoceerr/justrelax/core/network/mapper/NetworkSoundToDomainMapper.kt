package com.mustafakoceerr.justrelax.core.network.mapper

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound


/**
 * Sorumluluk: Network katmanından gelen DTO'ları Domain/Model katmanına çevirmek.
 * 'internal' olması, bu mapper'ın sadece bu modülün bir iç detayı olduğunu belirtir.
 */
internal class NetworkSoundToDomainMapper {

    /**
     * Tek bir NetworkSound nesnesini Sound modeline dönüştürür.
     */
    fun toModel(dto: NetworkSound): Sound {
        return Sound(
            id = dto.id,
            names = dto.names,
            categoryId = dto.category, // Düzeltme: categoryId değil, category
            iconUrl = dto.iconUrl,
            remoteUrl = dto.audioUrl,
            localPath = null, // Her zaman null
            isInitial = dto.isInitial,
            sizeBytes = dto.sizeBytes
        )
    }

    fun toModelList(dtos: List<NetworkSound>): List<Sound> {
        return dtos.map { toModel(it) }
    }
}