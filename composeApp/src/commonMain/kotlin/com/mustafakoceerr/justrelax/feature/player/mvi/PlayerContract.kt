package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound

data class PlayerState(
    // Aktif sesler ve ses seviyeleri
    val activeSounds: Map<String, Float> = emptyMap(),
    // Master play/pause durumu
    val isMasterPlaying: Boolean = true,
    // Çalan seslerin detaylı listesi (Sound objeleri - UI'da ikon göstermek için)
    // Bunu burada tutmak performans sağlar, her UI kendi hesaplamaz.
    val activeSoundDetails: List<Sound> = emptyList()
)

sealed interface PlayerIntent{
    data class ToggleSound(val sound: Sound): PlayerIntent
    data class ChangeVolume(val soundId: String, val volume: Float): PlayerIntent
    data object ToggleMasterPlayPause: PlayerIntent
    data object StopAll: PlayerIntent
}