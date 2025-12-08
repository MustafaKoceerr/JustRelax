package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SoundCategory(val id: String) {
    WATER("water"),
    NATURE("nature"),
    AIR("air"),
    CITY("city"),
    NOISE("noise");

    companion object {
        // Güvenli dönüşüm: Bulamazsa varsayılan (örn: NATURE) döner veya null.
        fun fromId(id: String): SoundCategory = entries.find { it.id == id } ?: NATURE
    }
}