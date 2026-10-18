package com.mustafakoceerr.justrelax.core.playback.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.playback.MEDIA_AUDIO_ATTRIBUTES
import kotlin.coroutines.resume
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** One looping ExoPlayer instance. All player access is confined to the main thread. */
internal class ExoSoundPlayer(
    private val context: Context
) : SoundPlayer {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var exoPlayer: ExoPlayer? = null
    private var targetVolume: Float = AudioDefaults.BASE_VOLUME

    override suspend fun prepare(clip: String, volume: Float) = withContext(Dispatchers.Main) {
        releasePlayer()
        targetVolume = volume
        exoPlayer = ExoPlayer.Builder(context)
            .setLooper(Looper.getMainLooper())
            // Focus and the wake lock belong to SoundscapePlayer, once for the whole mix; if every
            // layer handled focus, the mix's own players would take it from each other.
            .setAudioAttributes(MEDIA_AUDIO_ATTRIBUTES, /* handleAudioFocus = */ false)
            .setWakeMode(C.WAKE_MODE_NONE)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(clip))
                this.volume = 0f
                prepare()
            }
    }

    override suspend fun play(fadeInDurationMs: Long, startAtFraction: Float) = withContext(Dispatchers.Main) {
        val player = exoPlayer ?: return@withContext
        // The duration is only known once the source is ready.
        if (player.awaitReady() && player.duration > 0) {
            player.seekTo((player.duration * startAtFraction.coerceIn(0f, 1f)).toLong())
        }
        player.play()
        fadeIn(fadeInDurationMs)
    }

    private suspend fun ExoPlayer.awaitReady(): Boolean {
        if (playbackState == Player.STATE_READY) return true
        return withTimeoutOrNull(READY_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            removeListener(this)
                            continuation.resume(Unit)
                        }
                    }
                }
                addListener(listener)
                continuation.invokeOnCancellation { mainHandler.post { removeListener(listener) } }
            }
        } != null
    }

    override suspend fun pause() = withContext(Dispatchers.Main) {
        exoPlayer?.pause()
        Unit
    }

    override suspend fun resume() = withContext(Dispatchers.Main) {
        // A pause may have interrupted the fade-in, so restore the full volume.
        exoPlayer?.volume = targetVolume
        exoPlayer?.play()
        Unit
    }

    override suspend fun release() = withContext(Dispatchers.Main) {
        releasePlayer()
    }

    override fun setVolume(volume: Float) {
        val apply = {
            targetVolume = volume
            exoPlayer?.volume = volume
        }
        if (Looper.myLooper() == Looper.getMainLooper()) apply() else mainHandler.post(apply)
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    private suspend fun fadeIn(durationMs: Long) {
        if (durationMs <= 0) {
            exoPlayer?.volume = targetVolume
            return
        }

        val delayTime = durationMs / FADE_STEPS
        for (step in 1..FADE_STEPS) {
            val player = exoPlayer ?: return
            // targetVolume is re-read each step so a slider change during the fade wins.
            player.volume = min(targetVolume * step / FADE_STEPS, targetVolume)
            delay(delayTime)
        }
        exoPlayer?.volume = targetVolume
    }

    private companion object {
        const val FADE_STEPS = 20
        const val READY_TIMEOUT_MS = 3_000L
    }
}
