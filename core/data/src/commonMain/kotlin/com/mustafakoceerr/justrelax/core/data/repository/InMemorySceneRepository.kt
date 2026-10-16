package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

/** Keeps the scene in memory for the app's lifetime, and remembers each sound's last volume and frequency. */
class InMemorySceneRepository(
    private val soundRepository: SoundRepository,
) : SceneRepository {

    private val _scene = MutableStateFlow(Scene.EMPTY)
    override val scene: StateFlow<Scene> = _scene.asStateFlow()

    private val volumes = MutableStateFlow<Map<String, Float>>(emptyMap())
    private val frequencies = MutableStateFlow<Map<String, EventFrequency>>(emptyMap())

    override suspend fun toggle(soundId: String): Resource<Unit> {
        if (_scene.value.layer(soundId) != null) {
            _scene.update { it.remove(soundId) }
            return Resource.Success(Unit)
        }

        val sound = soundRepository.getSound(soundId).first()
        if (sound == null || !sound.isDownloaded) return Resource.Error(AppError.Storage.FileNotFound())

        val layer = layerFor(sound, volumes.value[soundId] ?: AudioDefaults.BASE_VOLUME)
        var limitReached = false
        _scene.update { current ->
            limitReached = false
            when {
                current.layer(soundId) != null -> current
                current.canAdd(layer) -> current.add(layer)
                else -> current.also { limitReached = true }
            }
        }
        return if (limitReached) Resource.Error(AppError.Player.LimitExceeded(Scene.MAX_LAYERS)) else Resource.Success(Unit)
    }

    override fun setVolume(soundId: String, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        volumes.update { it + (soundId to clamped) }
        _scene.update { it.withVolume(soundId, clamped) }
    }

    override fun setFrequency(soundId: String, frequency: EventFrequency) {
        frequencies.update { it + (soundId to frequency) }
        _scene.update { it.withFrequency(soundId, frequency) }
    }

    override suspend fun replaceWith(volumes: Map<String, Float>) =
        replaceWith(volumes.map { (soundId, volume) -> Layer.Bed(soundId, volume.coerceIn(0f, 1f)) })

    override suspend fun replaceWith(scene: Scene) = replaceWith(scene.layers)

    private suspend fun replaceWith(layers: List<Layer>) {
        val soundsById = soundRepository.getSounds().first().associateBy { it.id }
        volumes.update { remembered -> remembered + layers.associate { it.soundId to it.volume } }
        frequencies.update { remembered ->
            remembered + layers.filterIsInstance<Layer.Event>().associate { it.soundId to it.frequency }
        }

        _scene.value = Scene(
            layers.mapNotNull { layer ->
                soundsById[layer.soundId]?.takeIf { it.isDownloaded }?.let { layerFor(it, layer.volume) }
            }.take(Scene.MAX_LAYERS)
        )
    }

    override fun clear() {
        _scene.value = Scene.EMPTY
    }

    private fun layerFor(sound: Sound, volume: Float): Layer = when (sound.kind) {
        SoundKind.BED -> Layer.Bed(sound.id, volume)
        SoundKind.EVENT -> Layer.Event(sound.id, volume, frequencies.value[sound.id] ?: sound.defaultFrequency)
    }
}
