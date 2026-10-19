package com.mustafakoceerr.justrelax.feature.library

import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.LocalizedSound

data class LibraryUiState(
    val isLoading: Boolean = true,
    val categories: Map<SoundCategory, List<LocalizedSound>> = emptyMap(),
    val selectedCategory: SoundCategory? = null,
    /** Which sounds play and how often, for the sound cards. Volumes are in [LibraryViewModel.volumes]. */
    val sceneCards: SceneCards = SceneCards(),
    val downloadingSoundIds: Set<String> = emptySet(),
    val userMessage: LibraryMessage? = null,
) {
    /** Nothing in the library yet, e.g. a first launch without internet. */
    val isEmpty: Boolean get() = !isLoading && categories.isEmpty()
}
