package com.mustafakoceerr.justrelax.core.playback

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C

/** Every player in the soundscape plays media (music content), so focus and routing treat it alike. */
internal val MEDIA_AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
    .setUsage(C.USAGE_MEDIA)
    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
    .build()
