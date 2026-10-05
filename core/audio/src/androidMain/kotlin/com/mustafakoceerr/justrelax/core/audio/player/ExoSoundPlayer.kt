package com.mustafakoceerr.justrelax.core.audio.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.min

/** One looping ExoPlayer instance. All player access is confined to the main thread. */
internal class ExoSoundPlayer(
    private val context: Context
) : SoundPlayer {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var exoPlayer: ExoPlayer? = null
    private var targetVolume: Float = AudioDefaults.BASE_VOLUME

    override suspend fun prepare(config: SoundConfig) = withContext(Dispatchers.Main) {
        releasePlayer()
        targetVolume = config.initialVolume
        exoPlayer = ExoPlayer.Builder(context)
            .setLooper(Looper.getMainLooper())
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(config.url))
                volume = 0f
                prepare()
            }
    }

    override suspend fun play(fadeInDurationMs: Long) = withContext(Dispatchers.Main) {
        exoPlayer?.play()
        fadeIn(fadeInDurationMs)
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
    }
}
