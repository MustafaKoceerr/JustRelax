package com.mustafakoceerr.justrelax.feature.library

import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class LibraryUiState(
    val isLoading: Boolean = true,
    val categories: Map<SoundCategory, List<LocalizedSound>> = emptyMap(),
    val selectedCategory: SoundCategory? = null,
    val playingSoundIds: Set<String> = emptySet(),
    val soundVolumes: Map<String, Float> = emptyMap(),
    /** How often each playing event sound occurs; beds are not in it. */
    val soundFrequencies: Map<String, EventFrequency> = emptyMap(),
    val downloadingSoundIds: Set<String> = emptySet(),
    val userMessage: UiText? = null,
)
