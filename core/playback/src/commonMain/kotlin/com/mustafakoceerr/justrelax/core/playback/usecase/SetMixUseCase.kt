package com.mustafakoceerr.justrelax.core.playback.usecase

import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlin.jvm.JvmName

/** Plays a mix as the current scene; sounds that are not downloaded are skipped. */
class SetMixUseCase(
    private val soundscapeEngine: SoundscapeEngine,
) {
    suspend operator fun invoke(mix: Map<LocalizedSound, Float>) =
        play(mix.map { (sound, volume) -> bed(sound.id, sound.localPath, volume) })

    @JvmName("invokeWithSounds")
    suspend operator fun invoke(mix: Map<Sound, Float>) =
        play(mix.map { (sound, volume) -> bed(sound.id, sound.localPath, volume) })

    private fun bed(soundId: String, path: String?, volume: Float): Layer.Bed? =
        if (path.isNullOrBlank()) null else Layer.Bed(soundId, clip = path, volume = volume.coerceIn(0f, 1f))

    private suspend fun play(layers: List<Layer.Bed?>) {
        soundscapeEngine.play(Scene(layers.filterNotNull().distinctBy { it.soundId }.take(Scene.MAX_LAYERS)))
    }
}
