package com.mustafakoceerr.justrelax.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SoundscapeService : Service() {

    private val audioMixer: AudioMixer by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var notificationManager: SoundscapeNotificationManager
    private lateinit var mediaSessionManager: MediaSessionManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        mediaSessionManager = MediaSessionManager(this, audioMixer, serviceScope)
        notificationManager = SoundscapeNotificationManager(this, mediaSessionManager.getSessionToken())

        audioMixer.state
            .onEach { state -> handleStateChange(state) }
            .launchIn(serviceScope)
    }

    private fun handleStateChange(state: GlobalMixerState) {
        mediaSessionManager.updateState(state)

        if (state.activeSounds.isNotEmpty()) {
            val notification = notificationManager.createNotification(state.isPlaying)
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> serviceScope.launch { audioMixer.pauseAll() }
            ACTION_PLAY -> serviceScope.launch { audioMixer.resumeAll() }
            ACTION_STOP -> serviceScope.launch { audioMixer.stopAll() }
        }
        return START_NOT_STICKY
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        serviceScope.launch {
            audioMixer.stopAll()
        }
        stopService()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSessionManager.release()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 888
        const val ACTION_PLAY = "com.mustafakoceerr.justrelax.service.PLAY"
        const val ACTION_PAUSE = "com.mustafakoceerr.justrelax.service.PAUSE"
        const val ACTION_STOP = "com.mustafakoceerr.justrelax.service.STOP"
    }
}