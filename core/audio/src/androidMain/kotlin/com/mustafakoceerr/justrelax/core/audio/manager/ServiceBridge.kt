package com.mustafakoceerr.justrelax.core.audio.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mustafakoceerr.justrelax.core.audio.service.JustRelaxService
import com.mustafakoceerr.justrelax.core.audio.R

class ServiceBridge(
    private val context: Context,
    private val onMasterToggle: () -> Unit,
    private val onStopAction: () -> Unit
) {
    private var service: JustRelaxService? = null
    private var isBound = false

    // CACHING
    private var lastIsPlaying: Boolean = false
    private var lastActiveCount: Int = 0

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as JustRelaxService.LocalBinder
            service = localBinder.getService()
            isBound = true

            service?.onMasterToggleAction = onMasterToggle
            service?.onStopAction = onStopAction

            pushUpdateToService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    fun startAndBind() {
        if (!isBound) {
            val intent = Intent(context, JustRelaxService::class.java)
            context.startForegroundService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun stopAndUnbind() {
        if (isBound) {
            service?.stopForegroundService()
            try {
                context.unbindService(connection)
            } catch (_: Exception) {
                // Servis ölmüş olabilir
            }
            isBound = false
            service = null
        }
    }

    fun updateNotification(isPlaying: Boolean, activeCount: Int) {
        lastIsPlaying = isPlaying
        lastActiveCount = activeCount
        pushUpdateToService()
    }

    private fun pushUpdateToService() {
        val text = if (lastActiveCount > 0) {
            context.getString(
                R.string.notification_active_sound_count,
                lastActiveCount
            )
        } else {
            context.getString(R.string.app_name)
        }

        service?.updateNotification(lastIsPlaying, text)
    }
}