package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository

/** Adds the sound to the scene, or removes it, and plays the scene if the sound is now in it. */
suspend fun SceneRepository.toggleAndPlay(soundId: String, playbackController: PlaybackController) {
    toggle(soundId)
    if (scene.value.layer(soundId) != null) playbackController.play()
}
