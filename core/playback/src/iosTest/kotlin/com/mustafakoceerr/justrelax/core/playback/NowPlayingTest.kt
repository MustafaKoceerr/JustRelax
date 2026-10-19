package com.mustafakoceerr.justrelax.core.playback

import platform.CoreGraphics.CGSizeMake
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoPropertyIsLiveStream
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.UIKit.UIImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
/** The lock screen and Control Center show what the Android notification shows. */
class NowPlayingTest {

    private val texts = NowPlayingTexts(title = "Calming Sounds", subtitle = "Enjoy the moment with Just Relax")

    @Test
    fun withNothingToPlay_thereIsNoNowPlayingEntry() {
        assertNull(nowPlayingInfo(hasContent = false, playing = false, texts = texts, artwork = null))
    }

    @Test
    fun aPlayingMix_showsTheNotificationsTitleAndSubtitle_asALiveStream() {
        val info = nowPlayingInfo(hasContent = true, playing = true, texts = texts, artwork = null)!!

        assertEquals("Calming Sounds", info[MPMediaItemPropertyTitle])
        assertEquals("Enjoy the moment with Just Relax", info[MPMediaItemPropertyArtist])
        // A soundscape has no end, so the system shows no progress bar.
        assertEquals(true, info[MPNowPlayingInfoPropertyIsLiveStream])
        assertEquals(1.0, info[MPNowPlayingInfoPropertyPlaybackRate])
    }

    @Test
    fun aPausedMix_staysWithRateZero() {
        val info = nowPlayingInfo(hasContent = true, playing = false, texts = texts, artwork = null)!!

        assertEquals(0.0, info[MPNowPlayingInfoPropertyPlaybackRate])
    }

    @Test
    fun theArtwork_isShownWhenThereIsOne() {
        val image = UIImage()
        val artwork = MPMediaItemArtwork(boundsSize = CGSizeMake(1.0, 1.0)) { _ -> image }

        val info = nowPlayingInfo(hasContent = true, playing = true, texts = texts, artwork = artwork)!!

        assertSame(artwork, info[MPMediaItemPropertyArtwork])
    }
}
