package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.flow.StateFlow

/**
 * AudioMixer'dan anlık GlobalMixerState'i dinlemek için kullanılır.
 * ViewModel bu use case'i kullanarak UI'ı günceller.
 *
 * 'invoke' operatörü sayesinde bu sınıf bir fonksiyon gibi çağrılabilir.
 * Örnek: getGlobalMixerStateUseCase()
 */
class GetGlobalMixerStateUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke(): StateFlow<GlobalMixerState> {
        return audioMixer.state
    }
}