package com.mustafakoceerr.justrelax.feature.home.util

import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.category_air
import justrelax.feature.home.generated.resources.category_asmr
import justrelax.feature.home.generated.resources.category_city
import justrelax.feature.home.generated.resources.category_music
import justrelax.feature.home.generated.resources.category_nature
import justrelax.feature.home.generated.resources.category_noise
import justrelax.feature.home.generated.resources.category_rain
import justrelax.feature.home.generated.resources.category_travel
import justrelax.feature.home.generated.resources.category_water
import justrelax.feature.home.generated.resources.category_zen
import org.jetbrains.compose.resources.StringResource

val SoundCategory.titleRes: StringResource
    get() = when (this) {
        SoundCategory.RAIN -> Res.string.category_rain
        SoundCategory.WATER -> Res.string.category_water
        SoundCategory.NATURE -> Res.string.category_nature
        SoundCategory.AIR -> Res.string.category_air
        SoundCategory.CITY -> Res.string.category_city
        SoundCategory.TRAVEL -> Res.string.category_travel
        SoundCategory.NOISE -> Res.string.category_noise
        SoundCategory.ASMR -> Res.string.category_asmr
        SoundCategory.ZEN -> Res.string.category_zen
        SoundCategory.MUSIC -> Res.string.category_music
    }

val SoundCategory.icon: ImageVector
    get() = defaultIcon.imageVector
