package com.mustafakoceerr.justrelax.core.playback

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher

/**
 * The thread the soundscape engine and its bed ExoPlayers run on, so their work (and Media3's
 * listener and analytics callbacks) stays off the main thread. The media session player stays on main.
 * Tests can pass the main looper to keep everything on one thread.
 */
class AudioThread(val looper: Looper) {

    val handler = Handler(looper)

    /** Runs right away when already on the audio thread, like Dispatchers.Main.immediate. */
    val dispatcher: CoroutineDispatcher = handler.asCoroutineDispatcher(NAME).immediate

    val isCurrent: Boolean get() = Looper.myLooper() == looper

    companion object {
        private const val NAME = "JustRelaxAudio"

        fun start(): AudioThread = AudioThread(HandlerThread(NAME).apply { start() }.looper)
    }
}
