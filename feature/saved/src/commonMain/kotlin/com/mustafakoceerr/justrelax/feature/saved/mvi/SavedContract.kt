package com.mustafakoceerr.justrelax.feature.saved.mvi

import com.mustafakoceerr.justrelax.core.model.SavedMix

data class SavedMixUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val icons: List<String>,
    val domainModel: SavedMix
)

// State artık tertemiz. Sadece liste ve yükleniyor bilgisi var.
// Hangi mix çalıyor, hangisi animasyon yapıyor bilgisi TUTULMAZ.
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
    data class ShowSnackbar(val message: String, val actionLabel: String?) : SavedEffect
}