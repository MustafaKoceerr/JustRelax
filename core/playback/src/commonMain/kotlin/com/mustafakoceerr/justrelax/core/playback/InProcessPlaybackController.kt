package com.mustafakoceerr.justrelax.core.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Drives a [SoundscapeSession] directly, for platforms without a media session service (iOS for now). */
internal class InProcessPlaybackController(
    private val session: SoundscapeSession,
    scope: CoroutineScope,
) : PlaybackController {

    override val isPlaying: StateFlow<Boolean> = session.state
        .map { it.playWhenReady && it.hasContent }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override fun play() = session.setPlayWhenReady(true)

    override fun pause() = session.setPlayWhenReady(false)
}
