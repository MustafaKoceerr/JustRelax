package com.mustafakoceerr.justrelax.core.playback.soundscape

import kotlin.test.Test
import kotlin.test.assertEquals

class StereoVolumesTest {

    @Test
    fun theCentre_playsBothSidesAtTheVolume() {
        assertEquals(StereoVolumes(0.8f, 0.8f), stereoVolumes(volume = 0.8f, pan = 0f))
    }

    @Test
    fun panningLowersOnlyTheOppositeSide() {
        assertEquals(StereoVolumes(0.4f, 0.8f), stereoVolumes(volume = 0.8f, pan = 0.5f))
        assertEquals(StereoVolumes(0.8f, 0.4f), stereoVolumes(volume = 0.8f, pan = -0.5f))
    }

    @Test
    fun fullyPanned_isSilentOnTheOtherSide() {
        assertEquals(StereoVolumes(0f, 1f), stereoVolumes(volume = 1f, pan = 1f))
        assertEquals(StereoVolumes(1f, 0f), stereoVolumes(volume = 1f, pan = -1f))
    }

    @Test
    fun outOfRangeValues_areClamped() {
        assertEquals(StereoVolumes(0f, 1f), stereoVolumes(volume = 1.5f, pan = 3f))
    }
}
