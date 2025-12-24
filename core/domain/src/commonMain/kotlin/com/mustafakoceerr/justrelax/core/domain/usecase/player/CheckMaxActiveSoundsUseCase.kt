package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.flow.first

class CheckMaxActiveSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    // Sadece bu sınıfı ilgilendiren sabit
    private companion object {
        const val MAX_ACTIVE_SOUNDS = 10
    }

    suspend operator fun invoke(): Boolean {
        val activeCount = audioMixer.playingSoundIds.first().size
        return activeCount >= MAX_ACTIVE_SOUNDS
    }

    // ViewModel'in hata mesajında doğru sayıyı göstermesi için
    fun getLimit(): Int = MAX_ACTIVE_SOUNDS
}