package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound

data class PlayerState(
    val activeSounds: Map<String, Float> = emptyMap(),
    val isMasterPlaying: Boolean = true,
    // Çalan seslerin detaylı listesi (Sound objeleri - UI'da ikon göstermek için)
    // Bunu burada tutmak performans sağlar, her UI kendi hesaplamaz.
    val activeSoundDetails: List<Sound> = emptyList(),
    // YENİ: Şu an indirilmekte olan seslerin ID listesi
    val downloadingSoundIds: Set<String> = emptySet()
)

sealed interface PlayerIntent{
    data class ToggleSound(val sound: Sound): PlayerIntent
    data class ChangeVolume(val soundId: String, val volume: Float): PlayerIntent
    data object ToggleMasterPlayPause: PlayerIntent
    data object StopAll: PlayerIntent
}