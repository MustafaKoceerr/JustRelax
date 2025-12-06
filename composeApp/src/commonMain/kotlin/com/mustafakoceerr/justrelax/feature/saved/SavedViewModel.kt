package com.mustafakoceerr.justrelax.feature.saved


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.database.domain.model.SavedMix
import com.mustafakoceerr.justrelax.core.database.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedEffect
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedIntent
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedState
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.PlaySavedMixUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SavedViewModel(
    private val savedMixRepository: SavedMixRepository,
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager,
    private val playSavedMixUseCase: PlaySavedMixUseCase // YENİ: UseCase eklendi
) : ScreenModel {

    private val _state = MutableStateFlow(SavedState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SavedEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var lastDeletedMix: SavedMix? = null

    init {
        processIntent(SavedIntent.LoadMixes)
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
        // TODO:
//        screenModelScope.launch {
//            combine(
//                savedMixRepository.getAllMixes(),
//                soundRepository.getSounds()
//            ) { savedMixes, allSounds ->
//                savedMixes.map { domainMix ->
//                    val icons = domainMix.sounds.mapNotNull { savedSound ->
//                        allSounds.find { it.id == savedSound.id }?.icon
//                    }.ifEmpty { listOf(Icons.Default.MusicNote) }
//
//                    SavedMixUiModel(
//                        id = domainMix.id,
//                        title = domainMix.name,
//                        date = formatEpoch(domainMix.dateEpoch),
//                        icons = icons,
//                        domainModel = domainMix
//                    )
//                }
//            }.collect { uiMixes ->
//                _state.update { it.copy(mixes = uiMixes, isLoading = false) }
//            }
//        }
    }

    // --- TEMİZLENMİŞ TOGGLE MANTIĞI ---
    private fun toggleMix(mixId: Long) {
        val currentId = _state.value.currentPlayingMixId

        if (currentId == mixId) {
            // Durdurma mantığı (Hala SoundManager üzerinden, çünkü basit)
            soundManager.stopAll()
            _state.update { it.copy(currentPlayingMixId = null) }
        } else {
            // Çalma mantığı (Artık UseCase üzerinden)
            val mixToPlay = _state.value.mixes.find { it.id == mixId }?.domainModel ?: return

            screenModelScope.launch {
                // Tüm karmaşık işi UseCase yapıyor
                playSavedMixUseCase(mixToPlay)

                // UI State güncelle
                _state.update { it.copy(currentPlayingMixId = mixId) }
            }
        }
    }

    private fun deleteMix(uiMix: SavedMixUiModel) {
        screenModelScope.launch {
            lastDeletedMix = uiMix.domainModel
            savedMixRepository.deleteMix(uiMix.id)

            if (_state.value.currentPlayingMixId == uiMix.id) {
                soundManager.stopAll()
                _state.update { it.copy(currentPlayingMixId = null) }
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

    @OptIn(ExperimentalTime::class)
    private fun formatEpoch(epochMillis: Long): String {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${date.day}.${date.month.number}.${date.year}"
    }
}