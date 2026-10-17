package com.mustafakoceerr.justrelax.core.audio.focus

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackInterruptionPolicyTest {

    private class FakeAudioFocus(var grant: Boolean = true) : AudioFocus {
        var requests = 0
        var abandons = 0
        override fun request(): Boolean {
            requests++
            return grant
        }
        override fun abandon() {
            abandons++
        }
    }

    private val mixer = FakeAudioMixer()
    private val focus = FakeAudioFocus()

    private fun TestScope.policy() = PlaybackInterruptionPolicy(mixer, focus, backgroundScope).also {
        it.start()
        runCurrent()
    }

    private fun TestScope.startPlaying() {
        mixer.setState(GlobalMixerState(isPlaying = true, activeSounds = listOf(SoundConfig("rain", "/rain.mp3"))))
        runCurrent()
    }

    private val isPlaying get() = mixer.state.value.isPlaying

    @Test
    fun startingPlayback_requestsFocusOnce() = runTest {
        policy()

        startPlaying()

        assertEquals(1, focus.requests)
    }

    @Test
    fun deniedFocus_pausesTheMix() = runTest {
        focus.grant = false // e.g. a phone call is in progress
        policy()

        startPlaying()

        assertFalse(isPlaying)
    }

    @Test
    fun permanentLoss_pauses_andRegainingFocusDoesNotResume() = runTest {
        val policy = policy()
        startPlaying()

        policy.onFocusLost(transient = false) // another app started music
        runCurrent()
        assertFalse(isPlaying)

        policy.onFocusGained()
        runCurrent()
        assertFalse(isPlaying)
    }

    @Test
    fun transientLossWhilePlaying_pauses_andResumesWhenFocusReturns() = runTest {
        val policy = policy()
        startPlaying()

        policy.onFocusLost(transient = true) // phone call
        runCurrent()
        assertFalse(isPlaying)

        policy.onFocusGained() // call ended
        runCurrent()
        assertTrue(isPlaying)
    }

    @Test
    fun transientLossWhileAlreadyPaused_doesNotStartPlaybackAfterwards() = runTest {
        val policy = policy()
        startPlaying()
        mixer.pauseAll()
        runCurrent()

        policy.onFocusLost(transient = true)
        policy.onFocusGained()
        runCurrent()

        assertFalse(isPlaying)
    }

    @Test
    fun userPausingDuringAnInterruption_winsOverAutoResume() = runTest {
        val policy = policy()
        startPlaying()
        policy.onFocusLost(transient = true)
        runCurrent()

        mixer.resumeAll() // user taps play during the call...
        runCurrent()
        mixer.pauseAll() // ...and pauses again
        runCurrent()
        policy.onFocusGained()
        runCurrent()

        assertFalse(isPlaying)
    }

    @Test
    fun becomingNoisy_pauses_andDoesNotAutoResume() = runTest {
        val policy = policy()
        startPlaying()

        policy.onBecomingNoisy() // headphones unplugged
        runCurrent()
        policy.onFocusGained()
        runCurrent()

        assertFalse(isPlaying)
    }

    @Test
    fun playingAgainAfterPermanentLoss_requestsFocusAgain() = runTest {
        val policy = policy()
        startPlaying()
        policy.onFocusLost(transient = false)
        runCurrent()

        mixer.resumeAll()
        runCurrent()

        assertEquals(2, focus.requests)
    }

    @Test
    fun stoppingTheMix_abandonsFocus() = runTest {
        policy()
        startPlaying()

        mixer.stopAll()
        runCurrent()

        assertEquals(1, focus.abandons)
    }

    @Test
    fun stoppingThePolicy_abandonsHeldFocus() = runTest {
        val policy = policy()
        startPlaying()

        policy.stop()

        assertEquals(1, focus.abandons)
    }
}
