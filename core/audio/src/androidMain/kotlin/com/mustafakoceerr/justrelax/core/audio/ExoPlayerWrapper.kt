package com.mustafakoceerr.justrelax.core.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

internal class ExoPlayerWrapper(
    private val context: Context,
    private val scope: CoroutineScope,
    private val dispatchers: DispatcherProvider
) {
    private companion object {
        const val TAG = "ExoPlayerWrapper"
    }

    private var exoPlayer: ExoPlayer? = null
    private var fadeJob: Job? = null
    private var currentConfig: SoundConfig? = null
    private var targetVolume: Float = 0f

    // Loglarda hangi ses olduğunu anlamak için ID'yi alıyoruz
    private val soundId: String get() = currentConfig?.id ?: "Unknown"

    fun prepare(config: SoundConfig) {
        this.currentConfig = config
        this.targetVolume = config.initialVolume

        if (exoPlayer == null) {
            Log.d(TAG, "[$soundId] prepare: Building new ExoPlayer instance.")
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(config.url))
                volume = config.initialVolume
                prepare()
            }
        } else {
            Log.d(TAG, "[$soundId] prepare: ExoPlayer already exists.")
        }
    }

    fun play() {
        val config = currentConfig ?: return
        val player = exoPlayer ?: return

        if (player.isPlaying) {
            Log.d(TAG, "[$soundId] play ignored: Already playing.")
            return
        }

        Log.d(TAG, "[$soundId] play called. Fade: ${config.fadeInDurationMs}ms")

        if (config.fadeInDurationMs > 0) {
            player.volume = 0f
            player.play()
            startFade(from = 0f, to = targetVolume, duration = config.fadeInDurationMs)
        } else {
            player.volume = targetVolume
            player.play()
        }
    }

    fun stop(onRelease: () -> Unit) {
        val player = exoPlayer
        val config = currentConfig

        Log.d(TAG, "[$soundId] stop called.")

        if (player == null || !player.isPlaying) {
            Log.d(TAG, "[$soundId] stop: Player null or not playing. Killing immediately.")
            kill()
            onRelease()
            return
        }

        val fadeOutDuration = config?.fadeOutDurationMs ?: 0L

        if (fadeOutDuration > 0) {
            Log.d(TAG, "[$soundId] stop: Starting fade-out ($fadeOutDuration ms).")
            val currentVol = player.volume
            startFade(from = currentVol, to = 0f, duration = fadeOutDuration) {
                Log.d(TAG, "[$soundId] Fade-out complete. Killing.")
                kill()
                onRelease()
            }
        } else {
            Log.d(TAG, "[$soundId] stop: No fade-out duration. Killing.")
            kill()
            onRelease()
        }
    }

    fun kill() {
        Log.d(TAG, "[$soundId] kill called.")

        fadeJob?.cancel()
        fadeJob = null

        if (exoPlayer != null) {
            try {
                Log.d(TAG, "[$soundId] Releasing ExoPlayer...")
                exoPlayer?.stop()
                exoPlayer?.release()
                Log.d(TAG, "[$soundId] ExoPlayer released successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "[$soundId] Error releasing ExoPlayer: ${e.message}")
            } finally {
                exoPlayer = null
            }
        } else {
            Log.w(TAG, "[$soundId] kill ignored: ExoPlayer is already null.")
        }
    }

    fun setVolume(volume: Float) {
        targetVolume = volume
        if (fadeJob == null) {
            exoPlayer?.volume = volume
        }
    }

    fun pause() {
        Log.d(TAG, "[$soundId] pause called.")
        fadeJob?.cancel()
        exoPlayer?.pause()
    }

    fun resume() {
        Log.d(TAG, "[$soundId] resume called.")
        exoPlayer?.volume = targetVolume
        exoPlayer?.play()
    }

    private fun startFade(from: Float, to: Float, duration: Long, onComplete: (() -> Unit)? = null) {
        fadeJob?.cancel()
        fadeJob = scope.launch(dispatchers.main) {
            // Log.v(TAG, "[$soundId] Fading from $from to $to over $duration ms")
            val steps = 20
            val stepTime = duration / steps
            val volumeStep = (to - from) / steps

            var currentVol = from
            for (i in 1..steps) {
                if (!isActive) {
                    Log.d(TAG, "[$soundId] Fade job cancelled.")
                    return@launch
                }
                currentVol += volumeStep
                val safeVol = max(0f, min(1f, currentVol))
                exoPlayer?.volume = safeVol
                delay(stepTime)
            }
            exoPlayer?.volume = to
            fadeJob = null
            onComplete?.invoke()
        }
    }
}