package com.mustafakoceerr.justrelax.core.sound.domain.model


import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.generated.resources.category_air
import com.mustafakoceerr.justrelax.core.generated.resources.category_city
import com.mustafakoceerr.justrelax.core.generated.resources.category_nature
import com.mustafakoceerr.justrelax.core.generated.resources.category_noise
import com.mustafakoceerr.justrelax.core.generated.resources.category_water
import org.jetbrains.compose.resources.StringResource

enum class SoundCategory(val id: String, val titleRes: StringResource) {
    WATER("water", Res.string.category_water),
    NATURE("nature", Res.string.category_nature),
    AIR("air", Res.string.category_air),
    CITY("city", Res.string.category_city),
    NOISE("noise", Res.string.category_noise)
}