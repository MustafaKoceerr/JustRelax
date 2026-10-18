package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.playback.SoundscapeSession.PlayWhenReadyReason

/**
 * What the platform's audio session reports, for platforms that leave the reaction to the app
 * (iOS AVAudioSession). Android's Media3 managers already map these onto [SoundscapeSession].
 */
internal sealed interface AudioSessionEvent {
    /** Another app or a call took the audio; the system has already silenced ours. */
    data object InterruptionBegan : AudioSessionEvent

    /** @param shouldResume the system's hint that playback should continue, e.g. after a call */
    data class InterruptionEnded(val shouldResume: Boolean) : AudioSessionEvent

    /** The output in use went away, e.g. headphones were unplugged. */
    data object OutputLost : AudioSessionEvent
}

/** Follows the platform's default behaviour, the same as Media3 does on Android. */
internal fun SoundscapeSession.onAudioSessionEvent(event: AudioSessionEvent) {
    val playWhenReady = state.value.playWhenReady
    when (event) {
        AudioSessionEvent.InterruptionBegan -> setSuppressed(playWhenReady)
        is AudioSessionEvent.InterruptionEnded -> {
            setSuppressed(false)
            if (playWhenReady && !event.shouldResume) setPlayWhenReady(false, PlayWhenReadyReason.AUDIO_FOCUS_LOSS)
        }
        AudioSessionEvent.OutputLost -> if (playWhenReady) setPlayWhenReady(false, PlayWhenReadyReason.BECOMING_NOISY)
    }
}
