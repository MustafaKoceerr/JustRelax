package com.mustafakoceerr.justrelax.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedContract
import com.mustafakoceerr.justrelax.feature.saved.usecase.DeleteSavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.ObserveSavedMixesUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.RestoreSavedMixUseCase
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_undo
import justrelax.feature.saved.generated.resources.err_play_mix_failed
import justrelax.feature.saved.generated.resources.err_unknown
import justrelax.feature.saved.generated.resources.msg_mix_deleted
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedViewModel(
    private val observeSavedMixesUseCase: ObserveSavedMixesUseCase,
    private val playSavedMixUseCase: PlaySavedMixUseCase,
    private val deleteSavedMixUseCase: DeleteSavedMixUseCase,
    private val restoreSavedMixUseCase: RestoreSavedMixUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SavedContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<SavedContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var lastDeletedMix: SavedMix? = null
    private var playbackJob: Job? = null

    init {
        observeMixes()
    }

    fun onEvent(event: SavedContract.Event) {
        when (event) {
            is SavedContract.Event.LoadMixes -> observeMixes()
            is SavedContract.Event.PlayMix -> playMix(event.mixId)
            is SavedContract.Event.DeleteMix -> deleteMix(event.mix)
            is SavedContract.Event.UndoDelete -> undoDelete()
            is SavedContract.Event.CreateNewMix -> sendEffect(SavedContract.Effect.NavigateToMixer)
        }
    }

    private fun observeMixes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            observeSavedMixesUseCase().collectLatest { domainMixes ->
                val uiMixes = domainMixes.map { domainMix ->
                    SavedContract.SavedMixUiModel(
                        id = domainMix.id,
                        title = domainMix.name,
                        date = domainMix.createdAt,
                        icons = domainMix.sounds.keys.map { it.iconUrl },
                        domainModel = domainMix
                    )
                }
                _state.update { it.copy(mixes = uiMixes, isLoading = false) }
            }
        }
    }

    private fun playMix(mixId: Long) {
        playbackJob?.cancel()

        val mixToPlay = _state.value.mixes.find { it.id == mixId }?.domainModel ?: return

        playbackJob = viewModelScope.launch {
            try {
                playSavedMixUseCase(mixToPlay)
            } catch (e: Exception) {
                sendEffect(
                    SavedContract.Effect.ShowUndoSnackbar(
                        message = UiText.Resource(Res.string.err_play_mix_failed)
                    )
                )
            }
        }
    }

    private fun deleteMix(uiMix: SavedContract.SavedMixUiModel) {
        viewModelScope.launch {
            try {
                lastDeletedMix = uiMix.domainModel
                deleteSavedMixUseCase(uiMix.id)

                sendEffect(
                    SavedContract.Effect.ShowUndoSnackbar(
                        message = UiText.Resource(
                            resId = Res.string.msg_mix_deleted,
                            formatArgs = listOf(uiMix.title)
                        ),
                        actionLabel = UiText.Resource(Res.string.action_undo)
                    )
                )
            } catch (e: Exception) {
                sendEffect(SavedContract.Effect.ShowUndoSnackbar(UiText.Resource(Res.string.err_unknown)))
            }
        }
    }

    private fun undoDelete() {
        val mixToRestore = lastDeletedMix ?: return
        viewModelScope.launch {
            try {
                restoreSavedMixUseCase(mixToRestore)
                lastDeletedMix = null
            } catch (e: Exception) {
                sendEffect(SavedContract.Effect.ShowUndoSnackbar(UiText.Resource(Res.string.err_unknown)))
            }
        }
    }

    private fun sendEffect(effect: SavedContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}