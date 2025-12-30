package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * O anda çalmakta olan tüm sesleri duraklatır (Pause).
 * Bu işlem, state'i günceller ve bildirimdeki ikonun değişmesini tetikler.
 */
class PauseAllSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * Tüm sesleri duraklatır. Bu bir suspend fonksiyonudur ve
     * bir CoroutineScope (örn: viewModelScope) içinden çağrılmalıdır.
     */
    suspend operator fun invoke() {
        audioMixer.pauseAll()
    }
}