package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

interface MixerContract {

    /**
     * UI'ın anlık durumunu tutar.
     */
    data class State(
        val isGenerating: Boolean = false,
        val selectedSoundCount: Int = 4,
        val mixedSounds: List<Sound> = emptyList(),
        // 'isSaveDialogVisible' kaldırıldı (Player'a taşındı)
    )

    /**
     * Kullanıcı etkileşimleri (Event).
     */
    sealed interface Event {
        data class SelectSoundCount(val count: Int) : Event

        data object GenerateMix : Event

        data class ToggleSound(val soundId: String) : Event

        data class ChangeVolume(val soundId: String, val volume: Float) : Event

    }

    /**
     * Tek seferlik yan etkiler (Side Effects).
     */
    sealed interface Effect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}