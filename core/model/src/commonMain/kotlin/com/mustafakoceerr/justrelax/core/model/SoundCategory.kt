package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SoundCategory(val id: String) {
    WATER("WATER"),
    NATURE("NATURE"),
    AIR("AIR"),
    CITY("CITY"),
    NOISE("NOISE");

    companion object {
        // Güvenli dönüşüm: Bulamazsa varsayılan (örn: NATURE) döner veya null.
        fun fromId(id: String): SoundCategory = entries.find { it.id == id } ?: NATURE
    }
}