package com.mustafakoceerr.justrelax.feature.settings.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.TipRepository
import com.mustafakoceerr.justrelax.core.model.TipProduct
import com.mustafakoceerr.justrelax.core.model.TipResult
import com.mustafakoceerr.justrelax.core.model.TipTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupportUiState(
    val isLoading: Boolean = true,
    val tips: List<TipProduct> = emptyList(),
    /** The tip waiting for the store; no other can start meanwhile. */
    val purchasingTier: TipTier? = null,
    /** The tip just given, for the thank-you. */
    val thankedTier: TipTier? = null,
    val userMessage: SupportMessage? = null,
)

/** Tips for the developer through the store's in-app purchases. */
class SupportViewModel(
    private val tipRepository: TipRepository,
) : ViewModel() {

    private data class ScreenState(
        val purchasingTier: TipTier? = null,
        val thankedTier: TipTier? = null,
        val userMessage: SupportMessage? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<SupportUiState> = combine(tipRepository.products, screenState) { products, screen ->
        SupportUiState(
            isLoading = false,
            tips = products.sortedBy { it.tier.ordinal },
            purchasingTier = screen.purchasingTier,
            thankedTier = screen.thankedTier,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupportUiState())

    fun tip(tier: TipTier) {
        if (screenState.value.purchasingTier != null) return
        screenState.update { it.copy(purchasingTier = tier) }
        viewModelScope.launch {
            val result = tipRepository.purchase(tier)
            screenState.update {
                when (result) {
                    TipResult.Success -> it.copy(purchasingTier = null, thankedTier = tier)
                    TipResult.Cancelled -> it.copy(purchasingTier = null)
                    is TipResult.Failed -> it.copy(purchasingTier = null, userMessage = SupportMessage.PurchaseFailed)
                }
            }
        }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }
}
