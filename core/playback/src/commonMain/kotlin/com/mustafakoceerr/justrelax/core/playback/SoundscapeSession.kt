package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.playback.soundscape.PlayableLayer
import com.mustafakoceerr.justrelax.core.playback.soundscape.PlayableScene
import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * The platform-independent half of the player: it renders the scene from [SceneRepository] on the
 * [engine] and owns play/pause. The platform player (Android: a Media3 SimpleBasePlayer) maps its
 * commands and audio-focus decisions onto [setPlayWhenReady], [setSuppressed] and [setDuckVolume].
 */
internal class SoundscapeSession(
    private val sceneRepository: SceneRepository,
    soundRepository: SoundRepository,
    engineFactory: (CoroutineScope) -> SoundscapeEngine,
    private val sleepTimer: SleepTimer,
    context: CoroutineContext,
) {
    enum class PlayWhenReadyReason { USER, AUDIO_FOCUS_LOSS, BECOMING_NOISY, SLEEP_TIMER, SCENE_CLEARED }

    data class State(
        val hasContent: Boolean = false,
        val playWhenReady: Boolean = false,
        val reason: PlayWhenReadyReason = PlayWhenReadyReason.USER,
        val isSuppressed: Boolean = false,
    ) {
        val isRunning: Boolean get() = hasContent && playWhenReady && !isSuppressed
    }

    private val scope = CoroutineScope(context + SupervisorJob())
    private val engine = engineFactory(scope)

    private val _state = MutableStateFlow(State(hasContent = sceneRepository.scene.value.layers.isNotEmpty()))
    val state: StateFlow<State> = _state.asStateFlow()

    private val duckVolume = MutableStateFlow(1f)
    private val fadeVolume = MutableStateFlow(1f)
    private var fadeJob: Job? = null

    private val jobs = listOf(
        scope.launch {
            combine(sceneRepository.scene, soundRepository.getSounds(), ::resolve)
                .distinctUntilChanged()
                .conflate() // a slow apply should skip to the latest scene, never cancel half-way
                .collect { engine.apply(it) }
        },
        scope.launch {
            sceneRepository.scene
                .map { it.layers.isNotEmpty() }
                .distinctUntilChanged()
                .drop(1)
                .collect(::onContentChanged)
        },
        scope.launch {
            combine(_state.map { it.isRunning }, duckVolume, fadeVolume) { running, duck, fade -> running to duck * fade }
                .distinctUntilChanged()
                .collect { (running, volume) ->
                    // Never let a volume change be heard right before a pause.
                    if (running) {
                        engine.setMasterVolume(volume)
                        engine.setRunning(true)
                    } else {
                        engine.setRunning(false)
                        engine.setMasterVolume(volume)
                    }
                }
        },
        scope.launch { sleepTimer.expirations.collect { fadeOutAndPause() } },
    )

    fun setPlayWhenReady(playWhenReady: Boolean, reason: PlayWhenReadyReason = PlayWhenReadyReason.USER) {
        cancelFade()
        updatePlayWhenReady(playWhenReady, reason)
    }

    /** Paused for a moment by the platform (e.g. a phone call), keeping playWhenReady. */
    fun setSuppressed(suppressed: Boolean) = _state.update { it.copy(isSuppressed = suppressed) }

    fun setDuckVolume(volume: Float) {
        duckVolume.value = volume.coerceIn(0f, 1f)
    }

    /** Stops sound and frees the engine; the scene stays in the repository for the next session. */
    fun release() {
        cancelFade()
        jobs.forEach { it.cancel() }
        scope.launch {
            engine.stop()
            scope.cancel()
        }
    }

    private fun onContentChanged(hasContent: Boolean) {
        _state.update { it.copy(hasContent = hasContent) }
        if (!hasContent) {
            setPlayWhenReady(false, PlayWhenReadyReason.SCENE_CLEARED)
            sleepTimer.cancel()
        }
    }

    private fun fadeOutAndPause() {
        if (!_state.value.playWhenReady) return
        cancelFade()
        fadeJob = scope.launch {
            repeat(FADE_OUT_STEPS) { step ->
                delay(FADE_OUT_DURATION / FADE_OUT_STEPS)
                fadeVolume.value = 1f - (step + 1f) / FADE_OUT_STEPS
            }
            updatePlayWhenReady(false, PlayWhenReadyReason.SLEEP_TIMER)
            fadeVolume.value = 1f
        }
    }

    private fun cancelFade() {
        fadeJob?.cancel()
        fadeJob = null
        fadeVolume.value = 1f
    }

    private fun updatePlayWhenReady(playWhenReady: Boolean, reason: PlayWhenReadyReason) =
        _state.update { it.copy(playWhenReady = playWhenReady, reason = reason) }

    private fun resolve(scene: Scene, sounds: List<Sound>): PlayableScene {
        val paths = sounds.associate { it.id to it.localPath }
        return PlayableScene(
            scene.layers.mapNotNull { layer ->
                val path = paths[layer.soundId] ?: return@mapNotNull null
                when (layer) {
                    is Layer.Bed -> PlayableLayer.Bed(layer.soundId, path, layer.volume)
                    // A v1 sound has one file; the v2 catalog brings several clips per event.
                    is Layer.Event -> PlayableLayer.Event(layer.soundId, listOf(path), layer.volume, layer.frequency)
                }
            }
        )
    }

    companion object {
        val FADE_OUT_DURATION = 10.seconds
        private const val FADE_OUT_STEPS = 50
    }
}
