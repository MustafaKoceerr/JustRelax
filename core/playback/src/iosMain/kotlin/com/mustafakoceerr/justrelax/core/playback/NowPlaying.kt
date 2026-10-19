package com.mustafakoceerr.justrelax.core.playback

import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoPropertyIsLiveStream
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate

/** The words the Android notification shows: `notification_title` and `notification_subtitle`. */
internal data class NowPlayingTexts(val title: String, val subtitle: String)

/**
 * What the lock screen and Control Center show for the mix, the same as the Android notification.
 * A soundscape has no length, so it is a live stream: the system draws no progress bar.
 */
internal fun nowPlayingInfo(
    hasContent: Boolean,
    playing: Boolean,
    texts: NowPlayingTexts,
    artwork: MPMediaItemArtwork?,
): Map<Any?, Any>? {
    if (!hasContent) return null
    return buildMap {
        put(MPMediaItemPropertyTitle, texts.title)
        put(MPMediaItemPropertyArtist, texts.subtitle)
        put(MPNowPlayingInfoPropertyIsLiveStream, true)
        put(MPNowPlayingInfoPropertyPlaybackRate, if (playing) 1.0 else 0.0)
        artwork?.let { put(MPMediaItemPropertyArtwork, it) }
    }
}
