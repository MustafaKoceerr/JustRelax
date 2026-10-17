package com.mustafakoceerr.justrelax.core.audio.focus

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Platform audio focus (Android: AudioManager). One request covers the whole mix. */
interface AudioFocus {
    /** @return true if focus was granted. */
    fun request(): Boolean
    fun abandon()
}

/**
 * Decides how the mix reacts to interruptions, following the platform guidelines:
 * - another app takes audio focus for good (music, video) -> pause, never auto-resume
 * - a temporary interruption (phone call, assistant) -> pause, resume afterwards only if
 *   the mix was playing when it started and the user has not changed playback since
 * - headphones/Bluetooth disconnect ("becoming noisy") -> pause, never auto-resume
 * Ducking (notifications, navigation prompts) is left to the system.
 */
class PlaybackInterruptionPolicy(
    private val audioMixer: AudioMixer,
    private val audioFocus: AudioFocus,
    private val scope: CoroutineScope,
) {
    private var hasFocus = false
    private var resumeOnGain = false
    // Distinguishes our own pauses from the user's, so a user pause cancels auto-resume.
    private var pausedByPolicy = false
    private var observeJob: Job? = null

    fun start() {
        observeJob = scope.launch {
            audioMixer.state
                .map { MixStatus(isPlaying = it.isPlaying, hasSounds = it.activeSounds.isNotEmpty()) }
                .distinctUntilChanged()
                .collect(::onMixChanged)
        }
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
        releaseFocus()
    }

    fun onFocusLost(transient: Boolean) {
        val wasPlaying = audioMixer.state.value.isPlaying
        resumeOnGain = transient && wasPlaying
        if (!transient) hasFocus = false
        if (wasPlaying) pause()
    }

    fun onFocusGained() {
        if (!resumeOnGain) return
        resumeOnGain = false
        scope.launch { audioMixer.resumeAll() }
    }

    fun onBecomingNoisy() {
        resumeOnGain = false
        if (audioMixer.state.value.isPlaying) pause()
    }

    private fun onMixChanged(status: MixStatus) {
        when {
            !status.hasSounds -> releaseFocus()
            status.isPlaying -> {
                // Any (re)start of playback settles a pending interruption.
                resumeOnGain = false
                if (!hasFocus) {
                    hasFocus = audioFocus.request()
                    if (!hasFocus) pause()
                }
            }
            // Paused while an interruption is pending: the user's choice wins over auto-resume.
            else -> if (!pausedByPolicy) resumeOnGain = false
        }
        pausedByPolicy = false
    }

    private fun pause() {
        pausedByPolicy = true
        scope.launch { audioMixer.pauseAll() }
    }

    private fun releaseFocus() {
        resumeOnGain = false
        if (hasFocus) {
            hasFocus = false
            audioFocus.abandon()
        }
    }

    private data class MixStatus(val isPlaying: Boolean, val hasSounds: Boolean)
}
