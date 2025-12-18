package com.mustafakoceerr.justrelax.core.audio.manager

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Görevi: Sadece "Silence.mp3" dosyasını döngüde çalmak ve durumunu raporlamak.
 */
class MasterPlayer(
    private val context: Context,
    private val onIsPlayingChanged: (Boolean) -> Unit
) {
    private var exoPlayer: ExoPlayer? = null

    val isPlaying: Boolean
        get() = exoPlayer?.isPlaying == true

    fun prepareAndPlay() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                val rawId = context.resources.getIdentifier("silence", "raw", context.packageName)
                if (rawId != 0) {
                    val uri = Uri.parse("android.resource://${context.packageName}/$rawId")
                    setMediaItem(MediaItem.fromUri(uri))
                    prepare()
                }
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@MasterPlayer.onIsPlayingChanged(isPlaying)
                    }
                })
            }
        }
        if (!isPlaying) {
            exoPlayer?.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun play() {
        exoPlayer?.play()
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}