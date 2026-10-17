package com.mustafakoceerr.justrelax.core.media

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Previews a range with its own ExoPlayer, created on first use. It takes audio focus like any
 * player, so the scene pauses while the user listens and Media3 resumes it afterwards if allowed.
 */
internal class ExoClipPreviewer(private val context: Context) : ClipPreviewer {

    private val playing = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = playing.asStateFlow()

    private var player: ExoPlayer? = null

    override fun play(path: String, range: TrimRange, loop: Boolean) {
        val player = player ?: createPlayer().also { player = it }
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(if ("://" in path) path else File(path).toURI().toString())
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(range.startMs)
                        .setEndPositionMs(range.endMs)
                        .build(),
                )
                .build(),
        )
        player.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        player.prepare()
        player.play()
    }

    override fun stop() {
        player?.stop()
    }

    override fun release() {
        player?.release()
        player = null
        playing.value = false
    }

    private fun createPlayer() = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
            /* handleAudioFocus = */ true,
        )
        .build()
        .apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playing.value = isPlaying
                }
            })
        }
}
