package com.mustafakoceerr.justrelax.feature.home

import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundUi
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class HomeUiState(
    val isLoading: Boolean = true,
    val categories: Map<SoundCategory, List<SoundUi>> = emptyMap(),
    val selectedCategory: SoundCategory? = null,
    val playingSoundIds: Set<String> = emptySet(),
    val soundVolumes: Map<String, Float> = emptyMap(),
    val downloadingSoundIds: Set<String> = emptySet(),
    val userMessage: UiText? = null,
)
