package com.mustafakoceerr.justrelax.core.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.R
import androidx.compose.ui.res.stringResource

/** The category's name in the app's language. */
@Composable
fun SoundCategory.title(): String = stringResource(titleRes)

@get:StringRes
private val SoundCategory.titleRes: Int
    get() = when (this) {
        SoundCategory.RAIN -> R.string.category_rain
        SoundCategory.WATER -> R.string.category_water
        SoundCategory.NATURE -> R.string.category_nature
        SoundCategory.AIR -> R.string.category_air
        SoundCategory.CITY -> R.string.category_city
        SoundCategory.TRAVEL -> R.string.category_travel
        SoundCategory.NOISE -> R.string.category_noise
        SoundCategory.ASMR -> R.string.category_asmr
        SoundCategory.ZEN -> R.string.category_zen
        SoundCategory.MUSIC -> R.string.category_music
    }
