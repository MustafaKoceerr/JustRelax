package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.audio.soundscape.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.audio.soundscape.SoundscapeState
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Simulates the engine's state transitions without playing anything. */
class FakeSoundscapeEngine : SoundscapeEngine {

    private val _state = MutableStateFlow(SoundscapeState())
    override val state: StateFlow<SoundscapeState> = _state

    var addResult: Resource<Unit> = Resource.Success(Unit)
    val addedLayers = mutableListOf<Layer>()

    fun setState(state: SoundscapeState) {
        _state.value = state
    }

    /** Convenience: a playing scene made of beds with the given ids. */
    fun playBeds(vararg ids: String) = setState(
        SoundscapeState(isPlaying = true, scene = Scene(ids.map { Layer.Bed(it, clip = "/sounds/$it.ogg", volume = 0.5f) }))
    )

    override suspend fun addLayer(layer: Layer): Resource<Unit> {
        addedLayers += layer
        if (addResult is Resource.Success && _state.value.scene.canAdd(layer)) {
            _state.update { it.copy(isPlaying = true, scene = it.scene.add(layer)) }
        }
        return addResult
    }

    override suspend fun removeLayer(soundId: String) {
        _state.update { current ->
            val remaining = current.scene.remove(soundId)
            current.copy(scene = remaining, isPlaying = current.isPlaying && remaining.layers.isNotEmpty())
        }
    }

    override suspend fun play(scene: Scene) {
        _state.update { it.copy(scene = scene, isPlaying = scene.layers.isNotEmpty()) }
    }

    override fun setVolume(soundId: String, volume: Float) {
        _state.update { it.copy(scene = it.scene.withVolume(soundId, volume)) }
    }

    override suspend fun setFrequency(soundId: String, frequency: EventFrequency) {
        _state.update { it.copy(scene = it.scene.withFrequency(soundId, frequency)) }
    }

    override suspend fun pauseAll() {
        _state.update { it.copy(isPlaying = false) }
    }

    override suspend fun resumeAll() {
        _state.update { it.copy(isPlaying = it.scene.layers.isNotEmpty()) }
    }

    override suspend fun stopAll() {
        _state.value = SoundscapeState()
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
