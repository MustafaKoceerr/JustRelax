package com.mustafakoceerr.justrelax.service

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mustafakoceerr.justrelax.MainActivity
import com.mustafakoceerr.justrelax.R
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Hosts the media session for the sound mixer. Media3 owns the notification and the
 * foreground state: it promotes the service while the mix plays and demotes it when paused.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private val audioMixer: AudioMixer by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelName(R.string.notification_channel_name)
                .build()
                .apply { setSmallIcon(R.drawable.ic_notification_small) }
        )

        val player = MixerSessionPlayer(audioMixer, serviceScope, mixMetadata())
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openAppIntent())
            .setMediaButtonPreferences(listOf(stopButton()))
            .build()
            // Media3 only manages the notification/foreground state of sessions added to the
            // service. Normally that happens when a MediaController binds; we are started with
            // startService, so the session is added explicitly.
            .also(::addSession)

        stopWhenMixBecomesEmpty()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** Keeps playing after the app is swiped away; a paused or empty mix is cleaned up. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            serviceScope.launch { audioMixer.stopAll() }
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun stopWhenMixBecomesEmpty() {
        serviceScope.launch {
            audioMixer.state
                .map { it.activeSounds.isNotEmpty() }
                .distinctUntilChanged()
                .drop(1) // only react to changes, not to the state at service start
                .filter { hasSounds -> !hasSounds }
                .collect { stopSelf() }
        }
    }

    private fun mixMetadata() = MediaMetadata.Builder()
        .setTitle(getString(R.string.notification_title))
        .setArtist(getString(R.string.notification_subtitle))
        .setArtworkUri(
            Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(packageName)
                // Built from the resource id (not its name) so the resource shrinker sees the reference.
                .appendPath(R.drawable.notification_artwork.toString())
                .build()
        )
        .build()

    private fun stopButton() = CommandButton.Builder(CommandButton.ICON_STOP)
        .setDisplayName(getString(R.string.action_stop))
        .setPlayerCommand(Player.COMMAND_STOP)
        .build()

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}
