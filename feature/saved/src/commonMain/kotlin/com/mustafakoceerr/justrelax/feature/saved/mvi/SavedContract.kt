package com.mustafakoceerr.justrelax.feature.saved.mvi

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class SavedMixUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val icons: List<String>,
    val domainModel: SavedMix
)

data class SavedState(
    val isLoading: Boolean = true,
    val mixes: List<SavedMixUiModel> = emptyList()
)

sealed interface SavedIntent {
    data object LoadMixes : SavedIntent
    data class PlayMix(val mixId: Long) : SavedIntent
    data class DeleteMix(val mix: SavedMixUiModel) : SavedIntent
    data object UndoDelete : SavedIntent
    data object CreateNewMix : SavedIntent
}

sealed interface SavedEffect {
    data object NavigateToMixer : SavedEffect

    // String yerine UiText kullanıyoruz.
    // ActionLabel genelde sabittir (UNDO/GERİ AL) ama onu da UiText yapabiliriz.
    data class ShowDeleteSnackbar(
        val message: UiText,
        val actionLabel: UiText? = null
    ) : SavedEffect}