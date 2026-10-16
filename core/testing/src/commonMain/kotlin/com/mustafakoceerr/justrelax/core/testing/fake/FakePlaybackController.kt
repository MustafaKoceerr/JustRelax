package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow

class FakePlaybackController : PlaybackController {
    override val isPlaying = MutableStateFlow(false)

    var playCount = 0
        private set

    override fun play() {
        playCount++
        isPlaying.value = true
    }

    override fun pause() {
        isPlaying.value = false
    }
}
