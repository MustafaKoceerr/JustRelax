package com.mustafakoceerr.justrelax.feature.saved

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.model.SavedMix
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedEffect
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedIntent
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedState
import com.mustafakoceerr.justrelax.feature.saved.usecase.ObserveSavedMixesUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedViewModel(
    private val savedMixRepository: SavedMixRepository,
    private val soundManager: SoundManager,
    private val playSavedMixUseCase: PlaySavedMixUseCase,
    private val observeSavedMixesUseCase: ObserveSavedMixesUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(SavedState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SavedEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var lastDeletedMix: SavedMix? = null

    init {
        processIntent(SavedIntent.LoadMixes)
        observePlaybackState()
    }

    private fun observePlaybackState() {
        screenModelScope.launch {
            soundManager.state.collectLatest { audioState ->
                _state.update {
                    it.copy(
                        // YENİ: -1 kontrolü
                        currentPlayingMixId = if (audioState.isMasterPlaying && audioState.activeMixId != -1L) {
                            audioState.activeMixId
                        } else {
                            -1 // Çalmıyorsa veya manuel değiştirildiyse -1
                        }
                    )
                }
            }
        }
    }

    fun processIntent(intent: SavedIntent) {
        when (intent) {
            is SavedIntent.LoadMixes -> observeMixes()
            is SavedIntent.PlayMix -> toggleMix(intent.mixId)
            is SavedIntent.DeleteMix -> deleteMix(intent.mix)
            is SavedIntent.UndoDelete -> undoDelete()
            is SavedIntent.CreateNewMix -> {
                screenModelScope.launch { _effect.send(SavedEffect.NavigateToMixer) }
            }
        }
    }

    private fun observeMixes() {
        screenModelScope.launch {
            observeSavedMixesUseCase().collect { uiMixes ->
                _state.update { it.copy(mixes = uiMixes, isLoading = false) }
            }
        }
    }

    private fun toggleMix(mixId: Long) {
        // YENİ: -1 kontrolü
        val currentActiveId = soundManager.state.value.activeMixId
        val isPlaying = soundManager.state.value.isMasterPlaying

        if (currentActiveId == mixId && isPlaying) {
            soundManager.stopAll()
        } else {
            val mixToPlay = _state.value.mixes.find { it.id == mixId }?.domainModel ?: return
            screenModelScope.launch {
                playSavedMixUseCase(mixToPlay)
                _state.update { it.copy(currentPlayingMixId = mixId) }
            }
        }
    }

    private fun deleteMix(uiMix: SavedMixUiModel) {
        screenModelScope.launch {
            lastDeletedMix = uiMix.domainModel
            savedMixRepository.deleteMix(uiMix.id)

            // YENİ: -1 kontrolü
            if (_state.value.currentPlayingMixId == uiMix.id) {
                soundManager.stopAll()
            }

            _effect.send(SavedEffect.ShowSnackbar("${uiMix.title} silindi", "Geri Al"))
        }
    }

    private fun undoDelete() {
        val mixToRestore = lastDeletedMix ?: return
        screenModelScope.launch {
            val soundMap = mixToRestore.sounds.associate { it.id to it.volume }
            savedMixRepository.saveMix(mixToRestore.name, soundMap)
            lastDeletedMix = null
        }
    }
}