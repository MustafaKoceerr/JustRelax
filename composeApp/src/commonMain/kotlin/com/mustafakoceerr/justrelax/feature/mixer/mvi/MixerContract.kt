package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound

// 1. STATE
data class MixerState(
    val selectedCount: Int = 5,
    val isLoading: Boolean = false,
    // UI'da hangi seslerin seçildiğini göstermek için (Volume bilgisi SoundManager'da tutuluyor ama burada da görsel için tutabiliriz)
    val mixedSounds: List<Sound> = emptyList()
)

// intent
sealed interface MixerIntent{
    data class SelectCount(val count: Int): MixerIntent
    data object CreateMix: MixerIntent
    data object SaveMix: MixerIntent
}

// 3. EFFECT
sealed interface MixerEffect {
    // Örn: data class ShowToast(val message: String) : MixerEffect
}