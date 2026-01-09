package com.mustafakoceerr.justrelax.core.audio.data

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.min

internal class ExoPlayerWrapper(
    private val context: Context
) {

    private var exoPlayer: ExoPlayer? = null
    private var targetVolume: Float = AudioDefaults.BASE_VOLUME

    suspend fun play(config: SoundConfig) = withContext(Dispatchers.Main) {
        releasePlayer()
        targetVolume = config.initialVolume

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(config.url))
            volume = 0f
            prepare()
            play()
        }

        fadeIn(config.fadeInDurationMs)
    }

    suspend fun stop() = withContext(Dispatchers.Main) {
        releasePlayer()
    }

    suspend fun pause() = withContext(Dispatchers.Main) {
        exoPlayer?.pause()
    }

    suspend fun resume() = withContext(Dispatchers.Main) {
        exoPlayer?.play()
    }

    fun setVolume(volume: Float) {
        targetVolume = volume
        try {
            exoPlayer?.volume = volume
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun releasePlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

    private suspend fun fadeIn(durationMs: Long) {
        if (durationMs <= 0) {
            exoPlayer?.volume = targetVolume
            return
        }

        val steps = 20
        val delayTime = durationMs / steps
        val volumeStep = targetVolume / steps
        var currentVol = 0f

        for (i in 1..steps) {
            if (exoPlayer == null) break

            currentVol += volumeStep
            val safeVol = min(currentVol, targetVolume)

            exoPlayer?.volume = safeVol
            delay(delayTime)
        }

        if (exoPlayer != null) {
            exoPlayer?.volume = targetVolume
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun prepare(config: SoundConfig) {
        val player = withContext(Dispatchers.IO) {
            ExoPlayer.Builder(context)
                .setLooper(Looper.getMainLooper())
                .build()
        }

        withContext(Dispatchers.Main) {
            releasePlayer()
            exoPlayer = player.apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(config.url))
                volume = 0f
                prepare()
            }
            targetVolume = config.initialVolume
        }
    }

    suspend fun playFadeIn(fadeInDurationMs: Long) = withContext(Dispatchers.Main) {
        exoPlayer?.play()
        fadeIn(fadeInDurationMs)
    }
}