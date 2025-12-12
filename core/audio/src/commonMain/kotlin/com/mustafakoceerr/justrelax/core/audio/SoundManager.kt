package com.mustafakoceerr.justrelax.core.audio

import com.mustafakoceerr.justrelax.core.domain.manager.SoundController
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SoundManager(
    private val audioPlayer: AudioPlayer
) : SoundController {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(MixerState())
    val state = _state.asStateFlow()

    init {
        scope.launch {
            audioPlayer.isPlaying.collect { isPlaying ->
                _state.update {
                    it.copy(isMasterPlaying = isPlaying)
                }
            }
        }
    }

    suspend fun toggleSound(sound: Sound, initialVolume: Float = 0.5f) {
        val currentState = _state.value
        val isAlreadyActive = currentState.activeSounds.containsKey(sound.id)

        if (isAlreadyActive) {
            removeSound(sound.id)
        } else {
            addSound(sound, initialVolume)
        }
    }

    private suspend fun addSound(sound: Sound, volume: Float) {
        _state.update { current ->
            val newMap = current.activeSounds.toMutableMap()
            newMap[sound.id] = ActiveSound(sound, volume, volume)

            current.copy(
                activeSounds = newMap,
                isMasterPlaying = true,
                activeMixId = -1 // Manuel ekleme -> Mix bozuldu
            )
        }

        sound.localPath?.let { path ->
            audioPlayer.play(sound.id, path, volume)
        }
    }

    private fun removeSound(soundId: String) {
        audioPlayer.stop(soundId)

        _state.update { current ->
            val newMap = current.activeSounds.toMutableMap()
            newMap.remove(soundId)

            current.copy(
                activeSounds = newMap,
                isMasterPlaying = newMap.isNotEmpty(),
                activeMixId = -1 // Manuel silme -> Mix bozuldu
            )
        }
    }

    // --- GERİ EKLENEN VE GÜNCELLENEN FONKSİYON ---
    fun onVolumeChange(soundId: String, newVolume: Float) {
        _state.update { state ->
            val sounds = state.activeSounds.toMutableMap()
            val activeSound = sounds[soundId] ?: return@update state

            sounds[soundId] = activeSound.copy(targetVolume = newVolume, currentVolume = newVolume)

            state.copy(
                activeSounds = sounds,
                activeMixId = -1 // YENİ: Ses seviyesi değişirse de mix bozulmuş sayılır (SavedScreen'deki seçim kalkar)
            )
        }
        audioPlayer.setVolume(soundId, newVolume)
    }
    // ---------------------------------------------

    suspend fun toggleMasterPlayPause() {
        if (_state.value.isMasterPlaying) {
            pauseAll()
        } else {
            resumeAll()
        }
    }

    private fun pauseAll() {
        audioPlayer.pauseAll()
        _state.update { it.copy(isMasterPlaying = false) }
    }

    private fun resumeAll() {
        if (_state.value.activeSounds.isEmpty()) return
        audioPlayer.resumeAll()
        _state.update { it.copy(isMasterPlaying = true) }
    }

    override fun stopAll() {
        scope.launch {
            _state.value.activeSounds.keys.forEach { soundId ->
                audioPlayer.stop(soundId)
            }
            _state.update {
                it.copy(
                    activeSounds = emptyMap(),
                    isMasterPlaying = false,
                    activeMixId = -1
                )
            }
        }
    }

    suspend fun setMix(mix: Map<Sound, Float>, mixId: Long = -1) {
        _state.value.activeSounds.keys.forEach { soundId ->
            audioPlayer.stop(soundId)
        }

        val newActiveSounds = mix.entries.associate { (sound, volume) ->
            sound.id to ActiveSound(
                sound = sound,
                targetVolume = volume,
                currentVolume = volume
            )
        }

        _state.update {
            it.copy(
                activeSounds = newActiveSounds,
                isMasterPlaying = true,
                activeMixId = mixId
            )
        }

        mix.forEach { (sound, volume) ->
            sound.localPath?.let { path ->
                audioPlayer.play(sound.id, path, volume)
            }
        }
    }
}