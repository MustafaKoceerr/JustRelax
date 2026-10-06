package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.model.SoundUi
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class MixerUiState(
    val selectedSoundCount: Int = MixerViewModel.DEFAULT_SOUND_COUNT,
    val isGenerating: Boolean = false,
    val mixedSounds: List<SoundUi> = emptyList(),
    val playingSoundIds: Set<String> = emptySet(),
    val soundVolumes: Map<String, Float> = emptyMap(),
    val userMessage: UiText? = null,
)
