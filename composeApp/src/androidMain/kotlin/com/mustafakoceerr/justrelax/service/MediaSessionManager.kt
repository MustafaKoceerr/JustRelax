package com.mustafakoceerr.justrelax.service

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Android Sistemi ile Uygulama arasındaki Medya Köprüsü.
 *
 * Sorumlulukları (SRP):
 * 1. MediaSessionCompat oluşturmak ve yönetmek.
 * 2. Medya butonlarını (Kulaklık, Bluetooth, Bildirim) dinleyip AudioMixer'a iletmek.
 * 3. Sisteme uygulamanın "Çalıyor" veya "Durdu" bilgisini vermek (PlaybackState).
 */
@OptIn(UnstableApi::class)
class MediaSessionManager
    (
    context: Context,
    private val audioMixer: AudioMixer
) {

    // "JustRelaxSession" etiketi loglarda görünür, debug için kolaylık sağlar.
    private val mediaSession = MediaSessionCompat(context, "JustRelaxSession")

    init {
        mediaSession.isActive = true

        // Medya butonlarından gelen komutları dinle
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                // Kullanıcı kulaklıktan veya bildirimden "Oynat" dedi.
                audioMixer.resumeAll()
                updateState(isPlaying = true)
            }

            override fun onPause() {
                // Kullanıcı "Duraklat" dedi.
                audioMixer.pauseAll()
                updateState(isPlaying = false)
            }

            override fun onStop() {
                // Kullanıcı "Kapat" dedi (Genelde bildirimdeki X butonu veya Swipe).
                audioMixer.stopAll()
                mediaSession.isActive = false
            }
        })
    }

    /**
     * Servis veya Mixer tarafından çağrılır.
     * Android sistemine "Şu anki durumumuz bu" diye rapor verir.
     */
    fun updateState(isPlaying: Boolean) {
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED

        // Hangi butonların aktif olduğunu belirtiyoruz.
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build()
        )
    }

    fun getSessionToken(): MediaSessionCompat.Token = mediaSession.sessionToken

    fun release() {
        mediaSession.isActive = false
        mediaSession.release()
    }
}