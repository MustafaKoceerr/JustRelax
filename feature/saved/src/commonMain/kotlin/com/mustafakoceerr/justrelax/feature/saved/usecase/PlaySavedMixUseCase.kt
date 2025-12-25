package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase


/**
 * Sorumluluk (SRP):
 * Veritabanından gelen bir SavedMix nesnesini, çalınabilir bir formata
 * (Map<Sound, Float>) dönüştürüp SetMixUseCase'e iletmek.
 */
class PlaySavedMixUseCase(
    private val setMixUseCase: SetMixUseCase
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        // SavedMix zaten Map<Sound, Float> tutuyor.
        // SetMixUseCase de tam olarak bunu bekliyor.
        // Dönüşüm yapmaya gerek yok, direkt paslıyoruz.
        setMixUseCase(savedMix.sounds)
    }
}