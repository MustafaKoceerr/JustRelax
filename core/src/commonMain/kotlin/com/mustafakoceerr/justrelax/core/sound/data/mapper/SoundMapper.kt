package com.mustafakoceerr.justrelax.core.sound.data.mapper
import com.mustafakoceerr.justrelax.core.database.SoundEntity
import kotlinx.serialization.json.Json

class SoundMapper (
    private val json: Json
){

    // Hangi dilde isim istediğimizi parametre olarak alıyoruz
    fun map(entity: SoundEntity, currentLanguageCode: String): Sound{
        // 1. JSON String'i Map'e çevir ({"en":"Rain", "tr":"Yağmur"})
        val namesMap: Map<String, String> = try {
            json.decodeFromString(entity.nameJson)
        }catch (e: Exception){
            emptyMap()
        }

        // 2. Doğru dili seç (Yoksa İngilizce, o da yoksa ilk bulduğunu al)
        val finalName = namesMap[currentLanguageCode]
            ?: namesMap["en"]
            ?:namesMap.values.firstOrNull()
            ?:"Unknown"

        return Sound(
            id = entity.id,
            name=finalName,
            category = mapCategory(entity.category),
            iconUrl = entity.iconUrl,
            audioUrl = entity.audioUrl,
            localPath = entity.localPath
            )
    }

    private fun mapCategory(categoryStr: String): SoundCategory{
        return try {
            SoundCategory.valueOf(categoryStr)
        }catch (e: Exception){
            SoundCategory.NATURE // Varsayılan.
        }
    }
}