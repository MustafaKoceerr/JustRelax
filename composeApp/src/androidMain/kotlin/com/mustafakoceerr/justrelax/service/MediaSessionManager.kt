package com.mustafakoceerr.justrelax.service

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MediaSessionManager(
    context: Context,
    private val audioMixer: AudioMixer,
    private val scope: CoroutineScope
) {
    private val mediaSession = MediaSessionCompat(context, "JustRelaxMediaSession")

    init {
        mediaSession.isActive = true

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                scope.launch { audioMixer.resumeAll() }
            }

            override fun onPause() {
                scope.launch { audioMixer.pauseAll() }
            }

            override fun onStop() {
                scope.launch { audioMixer.stopAll() }
            }
        })
    }

    fun updateState(state: GlobalMixerState) {
        val playbackState = if (state.isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(playbackState, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP
                )
                .build()
        )
    }

    fun getSessionToken(): MediaSessionCompat.Token = mediaSession.sessionToken

    fun release() {
        mediaSession.isActive = false
        mediaSession.release()
    }
}