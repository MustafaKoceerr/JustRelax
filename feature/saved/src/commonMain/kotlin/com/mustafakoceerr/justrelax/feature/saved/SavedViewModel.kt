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
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    /** The mix last played from here; it tells apart saved mixes made of the same sounds. */
    private val playedId = MutableStateFlow<Long?>(null)
    private var lastDeletedScene: SavedScene? = null
    private var playbackJob: Job? = null

    /** The sounds that play, or null while paused. Volume changes don't reach it. */
    private val playingSoundIds = combine(sceneRepository.scene, playbackController.isPlaying) { scene, isPlaying ->
        if (isPlaying) scene.soundIds else null
    }.distinctUntilChanged()

    val uiState: StateFlow<SavedUiState> = combine(
        savedSceneRepository.getSavedScenes(),
        soundRepository.getSounds(),
        languageController.currentLanguage,
        combine(selectedId, message, ::Pair),
        combine(playingSoundIds, playedId, ::Pair),
    ) { scenes, sounds, language, (selectedId, message), (playingSoundIds, playedId) ->
        val soundsById = sounds.associateBy { it.id }
        SavedUiState(
            isLoading = false,
            mixes = scenes.map { it.toItem(soundsById, language.code) },
            selected = scenes.find { it.id == selectedId }?.toDetail(soundsById, language.code),
            playingMixId = playingSoundIds?.let { ids -> playingMixId(scenes, ids, playedId) },
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
                playedId.value = saved.id
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message.value = SavedMessage.PlayFailed
            }
        }
    }

    /** Pauses the mix if it is the one playing, otherwise plays it. */
    fun togglePlay(mixId: Long) {
        viewModelScope.launch {
            val scenes = savedSceneRepository.getSavedScenes().first()
            val playingIds = sceneRepository.scene.value.soundIds.takeIf { playbackController.isPlaying.value }
            val isThisPlaying = playingIds != null && playingMixId(scenes, playingIds, playedId.value) == mixId
            if (isThisPlaying) playbackController.pause() else playMix(mixId)
        }
    }

    fun deleteMix(mixId: Long) {
        if (selectedId.value == mixId) selectedId.value = null
        viewModelScope.launch {
            val saved = findScene(mixId) ?: return@launch
            lastDeletedScene = saved
            savedSceneRepository.delete(saved.id)
            message.value = SavedMessage.Deleted(saved.name)
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

    /**
     * The saved mix with the sounds that play. When several have them, it is the one last played
     * from here, or none if that was not one of them.
     */
    private fun playingMixId(scenes: List<SavedScene>, playingSoundIds: Set<String>, playedId: Long?): Long? {
        val matches = scenes.filter { it.scene.soundIds == playingSoundIds }
        return matches.find { it.id == playedId }?.id ?: matches.singleOrNull()?.id
    }

    private suspend fun findScene(id: Long): SavedScene? =
        savedSceneRepository.getSavedScenes().first().find { it.id == id }

    private fun SavedScene.toItem(soundsById: Map<String, Sound>, languageCode: String): SavedMixItem {
        val sounds = scene.layers.mapNotNull { soundsById[it.soundId] }
        return SavedMixItem(
            id = id,
            title = name,
            date = createdAt,
            icons = sounds.map { it.icon },
            soundNames = sounds.map { it.getDisplayName(languageCode) },
        )
    }

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

/** A saved mix counts as playing whatever its volumes now are, as long as it has the same sounds. */
private val Scene.soundIds: Set<String> get() = layers.mapTo(mutableSetOf()) { it.soundId }
