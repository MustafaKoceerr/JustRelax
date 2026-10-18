package com.mustafakoceerr.justrelax.core.playback.soundscape

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class VoicePoolTest {

    private class Voice(val clip: String) {
        var playing = false
    }

    private val pool = VoicePool<Voice>(maxVoices = 2, isPlaying = { it.playing }, stop = { it.playing = false })

    private fun start(clip: String) = pool.voiceFor(clip) { Voice(clip) }.also { it.playing = true }

    @Test
    fun aFinishedVoice_isReusedForItsClip() {
        val first = start("owl.ogg")
        first.playing = false

        assertSame(first, start("owl.ogg"))
    }

    @Test
    fun aVoiceAddedUpFront_isUsedBeforeANewOneIsMade() {
        val preloaded = Voice("owl.ogg")
        pool.add("owl.ogg", preloaded)

        assertSame(preloaded, start("owl.ogg"))
    }

    @Test
    fun aClipThatIsStillSounding_getsAnotherVoice() {
        val first = start("owl.ogg")

        val second = start("owl.ogg")

        assertNotSame(first, second)
        assertTrue(first.playing)
    }

    @Test
    fun beyondTheLimit_theOldestVoiceIsStopped() {
        val oldest = start("owl_0.ogg")
        val middle = start("owl_1.ogg")

        start("owl_2.ogg")

        assertFalse(oldest.playing)
        assertTrue(middle.playing)
    }

    @Test
    fun removingClips_handsBackTheirVoices_andForgetsThem() {
        val owl = start("owl.ogg")
        start("bell.ogg")

        assertEquals(listOf(owl), pool.remove(listOf("owl.ogg")))
        assertEquals(listOf("bell.ogg"), pool.all().map { it.clip })
    }

    @Test
    fun sounding_listsOnlyTheVoicesStillPlaying() {
        val owl = start("owl.ogg")
        val bell = start("bell.ogg")
        bell.playing = false

        assertEquals(listOf(owl), pool.sounding())
    }
}
