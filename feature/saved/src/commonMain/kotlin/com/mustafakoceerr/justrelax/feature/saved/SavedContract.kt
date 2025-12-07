package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.database.domain.model.SavedMix as DomainSavedMix

// UI'da kullanacağımız Model (Senin UI kodundaki data class ile uyumlu olacak)
data class SavedMixUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val icons: List<String>, // ImageVector -> String (URL)
    // Orijinal domain modelini de tutuyoruz ki çalarken veya silerken kullanalım
    val domainModel: DomainSavedMix
)


// 1. State
data class SavedState(
    val isLoading: Boolean = true,
    val mixes: List<SavedMixUiModel> = emptyList(),
    val currentPlayingMixId: Long? = null // Şu an çalan mix'in ID'si
)

// 2. Intent (Kullanıcı eylemleri)
sealed interface SavedIntent {
    data object LoadMixes : SavedIntent
    data class PlayMix(val mixId: Long) : SavedIntent
    data class DeleteMix(val mix: SavedMixUiModel) : SavedIntent
    data object UndoDelete : SavedIntent
    data object CreateNewMix : SavedIntent
}

// 3. EFFECT (Tek seferlik olaylar)
sealed interface SavedEffect {
    data object NavigateToMixer : SavedEffect
    data class ShowSnackbar(val message: String, val actionLabel: String?) : SavedEffect
}