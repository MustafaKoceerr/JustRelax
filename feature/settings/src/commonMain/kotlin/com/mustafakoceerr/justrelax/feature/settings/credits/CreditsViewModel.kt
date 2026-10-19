package com.mustafakoceerr.justrelax.feature.settings.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.presentation.system.SystemLauncher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CreditItem(
    val title: String,
    val author: String,
    val license: License,
    val sourceUrl: String,
)

data class CreditsUiState(
    val isLoading: Boolean = true,
    val credits: List<CreditItem> = emptyList(),
)

/** The authors of the sounds in the library; CC-BY licenses require showing them. */
class CreditsViewModel(
    soundRepository: SoundRepository,
    private val systemLauncher: SystemLauncher,
) : ViewModel() {

    val uiState: StateFlow<CreditsUiState> = soundRepository.getSounds()
        .map { sounds ->
            CreditsUiState(
                isLoading = false,
                credits = sounds.mapNotNull { sound ->
                    sound.attribution?.let { CreditItem(it.title, it.author, sound.license, it.sourceUrl) }
                }.sortedBy { it.title.lowercase() },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CreditsUiState())

    fun openSource(credit: CreditItem) = systemLauncher.openUrl(credit.sourceUrl)
}
