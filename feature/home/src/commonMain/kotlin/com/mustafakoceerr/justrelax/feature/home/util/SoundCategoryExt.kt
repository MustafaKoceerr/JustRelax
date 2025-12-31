package com.mustafakoceerr.justrelax.feature.home.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.*
import org.jetbrains.compose.resources.StringResource

// 1. Title Extension
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

// 2. Icon Extension
val SoundCategory.icon: ImageVector
    get() = when (this) {
        SoundCategory.RAIN -> Icons.Rounded.Thunderstorm   // Yağmur ve Fırtına için
        SoundCategory.WATER -> Icons.Rounded.WaterDrop     // Su ve Okyanus için
        SoundCategory.NATURE -> Icons.Rounded.Forest       // Doğa için
        SoundCategory.AIR -> Icons.Rounded.Air             // Rüzgar için
        SoundCategory.CITY -> Icons.Rounded.Apartment      // Şehir binaları için
        SoundCategory.TRAVEL -> Icons.Rounded.DirectionsCar // Yolculuk (Genel araç) için
        SoundCategory.NOISE -> Icons.Rounded.GraphicEq     // Gürültü frekansları için
        SoundCategory.ASMR -> Icons.Rounded.Mic            // ASMR (Mikrofon/Kayıt) için
        SoundCategory.ZEN -> Icons.Rounded.SelfImprovement // Meditasyon yapan kişi ikonu
        SoundCategory.MUSIC -> Icons.Rounded.MusicNote     // Müzik notası
    }