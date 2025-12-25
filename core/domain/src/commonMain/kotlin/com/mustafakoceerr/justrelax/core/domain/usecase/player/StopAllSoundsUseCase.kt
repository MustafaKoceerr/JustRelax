package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Sorumluluk: Tüm sesleri durdurmak.
 * Kullanım yerleri: Timer bittiğinde, Stop butonu, Yeni Mix çalınacağı zaman.
 */
class StopAllSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke() {
        audioMixer.stopAll()
    }
}