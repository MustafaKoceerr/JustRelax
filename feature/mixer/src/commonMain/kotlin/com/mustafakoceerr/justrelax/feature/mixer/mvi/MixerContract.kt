package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class MixerState(
    val isGenerating: Boolean = false,
    val selectedSoundCount: Int = 4,
    val mixedSounds: List<Sound> = emptyList(),
    val isSaveDialogVisible: Boolean = false
)

sealed interface MixerIntent {
    data class SelectSoundCount(val count: Int) : MixerIntent
    data object GenerateMix : MixerIntent
    data class ToggleSound(val soundId: String) : MixerIntent
    data class ChangeVolume(val soundId: String, val volume: Float) : MixerIntent
    data object ShowSaveDialog : MixerIntent
    data object HideSaveDialog : MixerIntent
    data class SaveCurrentMix(val name: String) : MixerIntent
}

sealed interface MixerEffect {
    data class ShowSnackbar(val message: UiText) : MixerEffect
}