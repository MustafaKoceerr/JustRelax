package com.mustafakoceerr.justrelax.feature.home.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.category_air
import justrelax.feature.home.generated.resources.category_city
import justrelax.feature.home.generated.resources.category_nature
import justrelax.feature.home.generated.resources.category_noise
import justrelax.feature.home.generated.resources.category_water
import org.jetbrains.compose.resources.StringResource

// 1. Title Extension
val SoundCategory.titleRes: StringResource
    get() = when (this) {
        SoundCategory.WATER -> Res.string.category_water
        SoundCategory.NATURE -> Res.string.category_nature
        SoundCategory.AIR -> Res.string.category_air
        SoundCategory.CITY -> Res.string.category_city
        SoundCategory.NOISE -> Res.string.category_noise
    }

// 2. Icon Extension
val SoundCategory.icon: ImageVector
    get() = when (this) {
        SoundCategory.WATER -> Icons.Rounded.WaterDrop
        SoundCategory.NATURE -> Icons.Rounded.Forest
        SoundCategory.AIR -> Icons.Rounded.Air
        SoundCategory.CITY -> Icons.Rounded.Apartment
        SoundCategory.NOISE -> Icons.Rounded.GraphicEq
    }