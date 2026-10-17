package com.mustafakoceerr.justrelax.core.model

enum class SoundCategory(val id: String, val defaultIcon: SoundIcon) {
    RAIN("RAIN", SoundIcon.RAIN),
    WATER("WATER", SoundIcon.WATER_DROP),
    NATURE("NATURE", SoundIcon.FOREST),
    AIR("AIR", SoundIcon.WIND),
    CITY("CITY", SoundIcon.CITY),
    TRAVEL("TRAVEL", SoundIcon.CAR),
    NOISE("NOISE", SoundIcon.NOISE),
    ASMR("ASMR", SoundIcon.ASMR),
    ZEN("ZEN", SoundIcon.MEDITATION),
    MUSIC("MUSIC", SoundIcon.MUSIC);

    companion object {
        fun fromId(id: String): SoundCategory = entries.find { it.id == id } ?: NATURE
    }
}
