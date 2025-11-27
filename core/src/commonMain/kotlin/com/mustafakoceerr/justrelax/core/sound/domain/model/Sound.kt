package com.mustafakoceerr.justrelax.core.sound.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class Sound(
    val id: String,
    val nameRes: StringResource, // Çoklu dil desteği için
    val icon: ImageVector, // DEĞİŞTİ: DrawableResource -> ImageVector
    val audioFileName: String, // Dosya adı (örn: "water_rain_light.mp3")
    val category: SoundCategory
)