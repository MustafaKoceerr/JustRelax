package com.mustafakoceerr.justrelax.feature.saved

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedEffect
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedIntent
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedState
import com.mustafakoceerr.justrelax.feature.saved.usecase.DeleteSavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.ObserveSavedMixesUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.RestoreSavedMixUseCase
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_undo
import justrelax.feature.saved.generated.resources.msg_mix_deleted
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedScreenModel(
    private val observeSavedMixesUseCase: ObserveSavedMixesUseCase,
    private val playSavedMixUseCase: PlaySavedMixUseCase,
    private val deleteSavedMixUseCase: DeleteSavedMixUseCase,   // YENİ
    private val restoreSavedMixUseCase: RestoreSavedMixUseCase  // YENİ
) : ScreenModel {

    private val _state = MutableStateFlow(SavedState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SavedEffect>()
    val effect = _effect.receiveAsFlow()

    // Undo için geçici hafıza
    private var lastDeletedMix: SavedMix? = null

    init {
        observeMixes()
    }

    fun onIntent(intent: SavedIntent) {
        when (intent) {
            is SavedIntent.LoadMixes -> observeMixes()
            is SavedIntent.PlayMix -> playMix(intent.mixId)
            is SavedIntent.DeleteMix -> deleteMix(intent.mix)
            is SavedIntent.UndoDelete -> undoDelete()
            is SavedIntent.CreateNewMix -> sendEffect(SavedEffect.NavigateToMixer)
        }
    }

    private fun observeMixes() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            observeSavedMixesUseCase().collectLatest { uiMixes ->
                _state.update { it.copy(mixes = uiMixes, isLoading = false) }
            }
        }
    }

    private fun playMix(mixId: Long) {
        val mixToPlay = _state.value.mixes.find { it.id == mixId }?.domainModel ?: return
        screenModelScope.launch {
            playSavedMixUseCase(mixToPlay)
        }
    }

    private fun deleteMix(uiMix: SavedMixUiModel) {
        screenModelScope.launch {
            lastDeletedMix = uiMix.domainModel
            deleteSavedMixUseCase(uiMix.id)

            sendEffect(
                SavedEffect.ShowDeleteSnackbar(
                    // Resource kullanıyoruz ve argüman olarak başlığı veriyoruz
                    message = UiText.Resource(
                        resId = Res.string.msg_mix_deleted,
                        formatArgs = listOf(uiMix.title)
                    ),
                    // Buton metni
                    actionLabel = UiText.Resource(Res.string.action_undo)
                )
            )
        }
    }

    private fun undoDelete() {
        val mixToRestore = lastDeletedMix ?: return
        screenModelScope.launch {
            // UseCase üzerinden geri yükleme işlemi
            restoreSavedMixUseCase(mixToRestore)

            lastDeletedMix = null
        }
    }

    private fun sendEffect(effect: SavedEffect) {
        screenModelScope.launch {
            _effect.send(effect)
        }
    }
}