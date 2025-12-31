package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SoundCategory(val id: String) {
    RAIN("RAIN"),
    WATER("WATER"),
    NATURE("NATURE"),
    AIR("AIR"),
    CITY("CITY"),
    TRAVEL("TRAVEL"),
    NOISE("NOISE"),
    ASMR("ASMR"),
    ZEN("ZEN"),
    MUSIC("MUSIC");

    companion object {
        // Veritabanından gelen String ID'yi Enum'a çevirir.
        // Bilinmeyen bir kategori gelirse (örn: backend yeni kategori ekledi ama app eski),
        // varsayılan olarak NATURE döneriz ki app çökmesin.
        fun fromId(id: String): SoundCategory = entries.find { it.id == id } ?: NATURE
    }
}