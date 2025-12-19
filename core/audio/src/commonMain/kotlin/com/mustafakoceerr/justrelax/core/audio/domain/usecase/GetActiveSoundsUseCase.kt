package com.mustafakoceerr.justrelax.core.audio.domain.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager

class GetActiveSoundsUseCase(
    private val soundManager: SoundManager
) {
    operator fun invoke(): List<ActiveSoundInfo> {
        return soundManager.state.value.activeSounds.values.map { activeSound ->
            // Tam Sound objesi yerine sadece ihtiyacımız olan bilgileri içeren
            // ActiveSoundInfo objesi oluşturuyoruz.
            ActiveSoundInfo(
                id = activeSound.sound.id,
                name = activeSound.sound.name,
                volume = activeSound.targetVolume // Kullanıcının ayarladığı ses seviyesi
            )
        }
    }
}