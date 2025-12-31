package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * O anda çalmakta olan tüm sesleri durdurur ve ses listesini temizler.
 * Bu işlem, servisin de durmasını tetikler.
 */
class StopAllSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * Tüm sesleri durdurur. Bu bir suspend fonksiyonudur ve
     * bir CoroutineScope (örn: viewModelScope) içinden çağrılmalıdır.
     */
    suspend operator fun invoke() {
        audioMixer.stopAll()
    }
}