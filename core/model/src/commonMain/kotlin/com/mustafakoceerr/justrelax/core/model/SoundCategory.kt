package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SoundCategory(val id: String) {
    WATER("water"),
    NATURE("nature"),
    AIR("air"),
    CITY("city"),
    NOISE("noise")
}