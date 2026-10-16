package com.mustafakoceerr.justrelax.core.playback

import kotlinx.coroutines.flow.StateFlow

/**
 * Play and pause for the screens. What plays is the scene in SceneRepository; this only starts
 * and stops it (Android: through a Media3 MediaController, so system controls stay in sync).
 */
interface PlaybackController {
    /** True when the play/pause button should show "pause". */
    val isPlaying: StateFlow<Boolean>

    fun play()
    fun pause()
}
