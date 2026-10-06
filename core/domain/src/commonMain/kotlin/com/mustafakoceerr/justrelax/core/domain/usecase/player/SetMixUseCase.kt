package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene

/** Plays a mix as the current scene; sounds that are not downloaded are skipped. */
class SetMixUseCase(
    private val soundscapeEngine: SoundscapeEngine,
) {
    suspend operator fun invoke(mix: Map<LocalizedSound, Float>) {
        val layers = mix.mapNotNull { (sound, volume) ->
            val path = sound.localPath
            if (path.isNullOrBlank()) null else Layer.Bed(sound.id, clip = path, volume = volume.coerceIn(0f, 1f))
        }
        soundscapeEngine.play(Scene(layers.distinctBy { it.soundId }.take(Scene.MAX_LAYERS)))
    }
}
