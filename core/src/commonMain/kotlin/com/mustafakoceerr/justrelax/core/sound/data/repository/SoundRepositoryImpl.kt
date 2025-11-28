package com.mustafakoceerr.justrelax.core.sound.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.ModeFanOff
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.generated.resources.sound_cafe
import com.mustafakoceerr.justrelax.core.generated.resources.sound_campfire
import com.mustafakoceerr.justrelax.core.generated.resources.sound_fan
import com.mustafakoceerr.justrelax.core.generated.resources.sound_forest_birds
import com.mustafakoceerr.justrelax.core.generated.resources.sound_ocean
import com.mustafakoceerr.justrelax.core.generated.resources.sound_rain_light
import com.mustafakoceerr.justrelax.core.generated.resources.sound_rain_thunder
import com.mustafakoceerr.justrelax.core.generated.resources.sound_wind
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SoundRepositoryImpl : SoundRepository {

    private val mockSounds = listOf(
        // --- SU & YAĞMUR ---
        Sound(
            id = "water_1",
            nameRes = Res.string.sound_rain_light,
            icon = Icons.Rounded.WaterDrop, // Hazır ikon
            audioFileName = "water_rain_light.mp3",
            category = SoundCategory.WATER
        ),
        Sound(
            id = "water_2",
            nameRes = Res.string.sound_rain_thunder,
            icon = Icons.Rounded.Thunderstorm, // Hazır ikon
            audioFileName = "water_rain_thunder.mp3",
            category = SoundCategory.WATER
        ),
        Sound(
            id = "water_3",
            nameRes = Res.string.sound_ocean,
            icon = Icons.Rounded.Waves, // Hazır ikon
            audioFileName = "water_ocean_light.mp3",
            category = SoundCategory.WATER
        ),

        // --- DOĞA & ORMAN ---
        Sound(
            id = "nature_1",
            nameRes = Res.string.sound_forest_birds,
            icon = Icons.Rounded.Forest, // Hazır ikon
            audioFileName = "nature_forest_birds.mp3",
            category = SoundCategory.NATURE
        ),
        Sound(
            id = "nature_2",
            nameRes = Res.string.sound_campfire,
            icon = Icons.Rounded.LocalFireDepartment, // Ateş ikonu
            audioFileName = "nature_campfire.mp3",
            category = SoundCategory.NATURE
        ),

        // --- RÜZGAR ---
        Sound(
            id = "air_1",
            nameRes = Res.string.sound_wind,
            icon = Icons.Rounded.Air, // Rüzgar ikonu
            audioFileName = "air_wind.mp3",
            category = SoundCategory.AIR
        ),

        // --- ŞEHİR ---
        Sound(
            id = "city_1",
            nameRes = Res.string.sound_cafe,
            icon = Icons.Rounded.LocalCafe, // Kahve ikonu
            audioFileName = "city_cafe.mp3",
            category = SoundCategory.CITY
        ),

        // --- GÜRÜLTÜ ---
        Sound(
            id = "noise_1",
            nameRes = Res.string.sound_fan,
            icon = Icons.Rounded.ModeFanOff, // Pervane ikonu
            audioFileName = "noise_fan.mp3",
            category = SoundCategory.NOISE
        )
    )
    override fun getSounds(): Flow<List<Sound>> = flow{
        emit(mockSounds)
    }

    override suspend fun getSoundById(id: String): Sound? {
        return mockSounds.find { it.id == id }
    }
}