package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_air
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_asmr
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_city
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_music
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_nature
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_noise
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_rain
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_travel
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_water
import com.mustafakoceerr.justrelax.core.ui.generated.resources.category_zen
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The category's name in the app's language. */
@Composable
fun SoundCategory.title(): String = stringResource(titleRes)

private val SoundCategory.titleRes: StringResource
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
