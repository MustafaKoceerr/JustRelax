package com.mustafakoceerr.justrelax.service

import android.os.Looper
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MixerSessionPlayerTest {

    private val mixer = FakeAudioMixer()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val metadata = MediaMetadata.Builder().setTitle("Calming Sounds").build()
    private val player = MixerSessionPlayer(mixer, scope, metadata)

    @AfterTest
    fun tearDown() {
        player.release()
        scope.cancel()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun mixerPlaying(isPlaying: Boolean = true) = mixer.setState(
        GlobalMixerState(isPlaying = isPlaying, activeSounds = listOf(SoundConfig("rain", "/rain.mp3")))
    )

    @Test
    fun noActiveSounds_playerIsIdle() {
        idle()

        assertEquals(Player.STATE_IDLE, player.playbackState)
        assertEquals(0, player.mediaItemCount)
    }

    @Test
    fun activeSounds_playerIsReadyAndPlaying_withMixMetadata() {
        mixerPlaying()
        idle()

        assertEquals(Player.STATE_READY, player.playbackState)
        assertTrue(player.isPlaying)
        assertEquals("Calming Sounds", player.mediaMetadata.title)
    }

    @Test
    fun mixerPausedFromTheApp_isReflectedInPlayer() {
        mixerPlaying()
        idle()

        runBlocking { mixer.pauseAll() }
        idle()

        assertFalse(player.playWhenReady)
    }

    @Test
    fun pauseAndPlayFromNotification_controlTheMixer() {
        mixerPlaying()
        idle()

        player.pause()
        idle()
        assertFalse(mixer.state.value.isPlaying)
        assertFalse(player.playWhenReady)

        player.play()
        idle()
        assertTrue(mixer.state.value.isPlaying)
        assertTrue(player.playWhenReady)
    }

    @Test
    fun stopFromNotification_stopsAllSounds() {
        mixerPlaying()
        idle()

        player.stop()
        idle()

        assertTrue(mixer.state.value.activeSounds.isEmpty())
        assertEquals(Player.STATE_IDLE, player.playbackState)
    }
}
