package com.mustafakoceerr.justrelax.core.data.mapper

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import kotlinx.serialization.json.Json

class SoundMapper(private val json: Json) {
    fun map(entity: SoundEntity, currentLanguageCode: String): Sound {
        val namesMap: Map<String, String> = try {
            json.decodeFromString(entity.nameJson)
        } catch (e: Exception) {
            emptyMap()
        }

        val finalName = namesMap[currentLanguageCode]
            ?: namesMap["en"]
            ?: namesMap.values.firstOrNull()
            ?: "Unknown"

        return Sound(
            id = entity.id,
            name = finalName,
            category = mapCategory(entity.category),
            iconUrl = entity.iconUrl,
            audioUrl = entity.audioUrl,
            localPath = entity.localPath
        )
    }

    private fun mapCategory(categoryStr: String): SoundCategory {
        return try {
            SoundCategory.valueOf(categoryStr)
        } catch (e: Exception) {
            SoundCategory.NATURE
        }
    }
}