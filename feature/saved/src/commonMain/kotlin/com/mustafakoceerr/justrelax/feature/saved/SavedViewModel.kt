package com.mustafakoceerr.justrelax.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_undo
import justrelax.feature.saved.generated.resources.err_play_mix_failed
import justrelax.feature.saved.generated.resources.msg_mix_deleted
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val savedMixRepository: SavedMixRepository,
    private val playSavedMixUseCase: PlaySavedMixUseCase,
) : ViewModel() {

    private val message = MutableStateFlow<SavedMessage?>(null)
    private var lastDeletedMix: SavedMix? = null
    private var playbackJob: Job? = null

    val uiState: StateFlow<SavedUiState> = combine(
        savedMixRepository.getSavedMixes(),
        message,
    ) { mixes, message ->
        SavedUiState(isLoading = false, mixes = mixes.map { it.toItem() }, message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    fun playMix(mixId: Long) {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val mix = findMix(mixId) ?: return@launch
            try {
                playSavedMixUseCase(mix)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message.value = SavedMessage(UiText.Resource(Res.string.err_play_mix_failed))
            }
        }
    }

    fun deleteMix(mixId: Long) {
        viewModelScope.launch {
            val mix = findMix(mixId) ?: return@launch
            lastDeletedMix = mix
            savedMixRepository.deleteMix(mix.id)
            message.value = SavedMessage(
                text = UiText.Resource(Res.string.msg_mix_deleted, listOf(mix.name)),
                actionLabel = UiText.Resource(Res.string.action_undo),
            )
        }
    }

    fun undoDelete() {
        val mix = lastDeletedMix ?: return
        lastDeletedMix = null
        viewModelScope.launch {
            savedMixRepository.saveMix(mix.name, mix.sounds.entries.associate { (sound, volume) -> sound.id to volume })
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    private suspend fun findMix(mixId: Long): SavedMix? =
        savedMixRepository.getSavedMixes().first().find { it.id == mixId }

    private fun SavedMix.toItem() = SavedMixItem(
        id = id,
        title = name,
        date = createdAt,
        icons = sounds.keys.map { it.iconUrl },
    )
}
