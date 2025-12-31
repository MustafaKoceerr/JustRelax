package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Duraklatılmış olan tüm sesleri çalmaya devam ettirir (Resume).
 */
class ResumeAllSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * Tüm sesleri devam ettirir. Bu bir suspend fonksiyonudur ve
     * bir CoroutineScope (örn: viewModelScope) içinden çağrılmalıdır.
     */
    suspend operator fun invoke() {
        audioMixer.resumeAll()
    }
}