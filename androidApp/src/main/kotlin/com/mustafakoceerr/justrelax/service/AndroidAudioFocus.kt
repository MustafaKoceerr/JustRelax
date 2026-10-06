package com.mustafakoceerr.justrelax.service

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.mustafakoceerr.justrelax.core.audio.focus.AudioFocus

/**
 * A single audio focus request for the whole mix. Focus changes are delivered on the main thread.
 * Ducking is left to the system (setWillPauseWhenDucked(false)), so only real losses reach [onFocusChange].
 */
class AndroidAudioFocus(
    private val audioManager: AudioManager,
    onFocusChange: (Int) -> Unit,
) : AudioFocus {

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setWillPauseWhenDucked(false)
        .setOnAudioFocusChangeListener(onFocusChange, Handler(Looper.getMainLooper()))
        .build()

    override fun request(): Boolean =
        audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    override fun abandon() {
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}
