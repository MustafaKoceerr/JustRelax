package com.mustafakoceerr.justrelax.core.media

import kotlin.test.Test
import kotlin.test.assertEquals

class EdgeFadeTest {

    // 1 s at 1 kHz with 100 ms fades: frames 0..99 fade in, 900..999 fade out.
    private val fade = EdgeFade(durationMs = 1_000, fadeMs = 100)
    private val rate = 1_000

    @Test
    fun theClipRampsUpFromSilenceAndDownToSilence() {
        assertEquals(0f, fade.gainAt(0, rate))
        assertEquals(0.5f, fade.gainAt(50, rate))
        assertEquals(1f, fade.gainAt(500, rate))
        assertEquals(0.5f, fade.gainAt(950, rate), 0.011f)
        assertEquals(0f, fade.gainAt(1_000, rate))
    }

    @Test
    fun unityLastsUntilTheFadeOutStarts() {
        assertEquals(900, fade.unityUntil(500, rate))
    }

    @Test
    fun aClipShorterThanBothFadesFadesOverHalfItsLength() {
        val short = EdgeFade(durationMs = 100, fadeMs = 100)

        assertEquals(1f, short.gainAt(50, rate))
        assertEquals(0.5f, short.gainAt(25, rate))
    }

    @Test
    fun unityAlwaysEndsAfterTheCurrentFrameSoTheGainProcessorMovesOn() {
        assertEquals(51, EdgeFade(durationMs = 100, fadeMs = 100).unityUntil(50, rate))
    }
}
