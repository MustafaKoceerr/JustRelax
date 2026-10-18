package com.mustafakoceerr.justrelax.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.data.repository.SavedSceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.SavedScene
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.action_undo
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.err_play_mix_failed
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.msg_mix_deleted
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
    private val savedSceneRepository: SavedSceneRepository,
    soundRepository: SoundRepository,
    private val sceneRepository: SceneRepository,
    languageController: LanguageController,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val message = MutableStateFlow<SavedMessage?>(null)
    private val selectedId = MutableStateFlow<Long?>(null)
    private var lastDeletedScene: SavedScene? = null
    private var playbackJob: Job? = null

    val uiState: StateFlow<SavedUiState> = combine(
        savedSceneRepository.getSavedScenes(),
        soundRepository.getSounds(),
        languageController.currentLanguage,
        selectedId,
        message,
    ) { scenes, sounds, language, selectedId, message ->
        val soundsById = sounds.associateBy { it.id }
        SavedUiState(
            isLoading = false,
            mixes = scenes.map { it.toItem(soundsById) },
            selected = scenes.find { it.id == selectedId }?.toDetail(soundsById, language.code),
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    fun selectMix(mixId: Long?) {
        selectedId.value = mixId
    }

    fun playMix(mixId: Long) {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val saved = findScene(mixId) ?: return@launch
            try {
                sceneRepository.replaceWith(saved.scene)
                playbackController.play()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message.value = SavedMessage(UiText.Resource(Res.string.err_play_mix_failed))
            }
        }
    }

    fun deleteMix(mixId: Long) {
        if (selectedId.value == mixId) selectedId.value = null
        viewModelScope.launch {
            val saved = findScene(mixId) ?: return@launch
            lastDeletedScene = saved
            savedSceneRepository.delete(saved.id)
            message.value = SavedMessage(
                text = UiText.Resource(Res.string.msg_mix_deleted, listOf(saved.name)),
                actionLabel = UiText.Resource(Res.string.action_undo),
            )
        }
    }

    fun undoDelete() {
        val saved = lastDeletedScene ?: return
        lastDeletedScene = null
        viewModelScope.launch {
            savedSceneRepository.save(saved.name, saved.scene)
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    private suspend fun findScene(id: Long): SavedScene? =
        savedSceneRepository.getSavedScenes().first().find { it.id == id }

    private fun SavedScene.toItem(soundsById: Map<String, Sound>) = SavedMixItem(
        id = id,
        title = name,
        date = createdAt,
        icons = scene.layers.mapNotNull { soundsById[it.soundId]?.icon },
    )

    private fun SavedScene.toDetail(soundsById: Map<String, Sound>, languageCode: String) = SavedMixDetail(
        id = id,
        title = name,
        date = createdAt,
        sounds = scene.layers.mapNotNull { layer ->
            val sound = soundsById[layer.soundId] ?: return@mapNotNull null
            SavedSoundItem(
                name = sound.getDisplayName(languageCode),
                icon = sound.icon,
                volume = layer.volume,
                frequency = (layer as? Layer.Event)?.frequency,
            )
        },
    )
}
