package com.mustafakoceerr.justrelax.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cyclone
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Fireplace
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlutterDash
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Sailing
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WindPower
import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.core.model.SoundIcon

val SoundIcon.imageVector: ImageVector
    get() = when (this) {
        SoundIcon.RAIN -> Icons.Rounded.Umbrella
        SoundIcon.THUNDER -> Icons.Rounded.Thunderstorm
        SoundIcon.WATER_DROP -> Icons.Rounded.WaterDrop
        SoundIcon.WAVES -> Icons.Rounded.Waves
        SoundIcon.STREAM -> Icons.Rounded.Water
        SoundIcon.WIND -> Icons.Rounded.Air
        SoundIcon.STORM -> Icons.Rounded.Cyclone
        SoundIcon.SNOW -> Icons.Rounded.AcUnit
        SoundIcon.FIRE -> Icons.Rounded.LocalFireDepartment
        SoundIcon.FIREPLACE -> Icons.Rounded.Fireplace
        SoundIcon.FOREST -> Icons.Rounded.Forest
        SoundIcon.LEAF -> Icons.Rounded.Eco
        SoundIcon.GRASS -> Icons.Rounded.Grass
        SoundIcon.BIRD -> Icons.Rounded.FlutterDash
        SoundIcon.INSECT -> Icons.Rounded.BugReport
        SoundIcon.ANIMAL -> Icons.Rounded.Pets
        SoundIcon.NIGHT -> Icons.Rounded.NightsStay
        SoundIcon.CITY -> Icons.Rounded.Apartment
        SoundIcon.CAFE -> Icons.Rounded.LocalCafe
        SoundIcon.TRAIN -> Icons.Rounded.Train
        SoundIcon.CAR -> Icons.Rounded.DirectionsCar
        SoundIcon.PLANE -> Icons.Rounded.Flight
        SoundIcon.BOAT -> Icons.Rounded.Sailing
        SoundIcon.NOISE -> Icons.Rounded.GraphicEq
        SoundIcon.FAN -> Icons.Rounded.WindPower
        SoundIcon.KEYBOARD -> Icons.Rounded.Keyboard
        SoundIcon.ASMR -> Icons.Rounded.Headphones
        SoundIcon.BELL -> Icons.Rounded.NotificationsActive
        SoundIcon.MEDITATION -> Icons.Rounded.SelfImprovement
        SoundIcon.MUSIC -> Icons.Rounded.MusicNote
        SoundIcon.PIANO -> Icons.Rounded.Piano
        SoundIcon.MIC -> Icons.Rounded.Mic
    }
