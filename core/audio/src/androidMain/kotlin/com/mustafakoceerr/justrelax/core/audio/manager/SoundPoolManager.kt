package com.mustafakoceerr.justrelax.core.audio.manager

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

/**
 * Görevi: Sadece gerçek seslerin (Rain, Wind vb.) ExoPlayer instance'larını yönetmek.
 */
class SoundPoolManager(private val context: Context) {

    private val players = mutableMapOf<String, ExoPlayer>()

    fun play(soundId: String, path: String, volume: Float) {
        val player = players.getOrPut(soundId) {
            createPlayer()
        }

        // Eğer zaten çalıyorsa ve aynı dosya ise sadece sesini güncelle, reset atma
        // Ama basitlik için şimdilik yeniden yüklüyoruz.
        val uri = Uri.fromFile(File(path))
        player.setMediaItem(MediaItem.fromUri(uri))
        player.volume = volume
        player.prepare()
        player.play()
    }

    fun pause(soundId: String) {
        players[soundId]?.pause()
    }

    fun resume(soundId: String) {
        players[soundId]?.play()
    }

    fun stop(soundId: String) {
        players[soundId]?.release()
        players.remove(soundId)
    }

    fun setVolume(soundId: String, volume: Float) {
        players[soundId]?.volume = volume
    }

    fun pauseAll() {
        players.values.forEach { it.pause() }
    }

    fun resumeAll() {
        players.values.forEach { it.play() }
    }

    fun releaseAll() {
        players.values.forEach { it.release() }
        players.clear()
    }

    fun getActiveCount(): Int = players.size

    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
}