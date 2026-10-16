package com.mustafakoceerr.justrelax.core.playback.soundscape

data class StereoVolumes(val left: Float, val right: Float)

/**
 * A balance pan: the centre plays both sides at [volume], and panning towards one side lowers only
 * the other, so a clip is never louder or quieter than its layer just because of where it sits.
 *
 * @param pan from -1 (left) to 1 (right)
 */
fun stereoVolumes(volume: Float, pan: Float): StereoVolumes {
    val v = volume.coerceIn(0f, 1f)
    val p = pan.coerceIn(-1f, 1f)
    return StereoVolumes(left = v * (1f - p).coerceAtMost(1f), right = v * (1f + p).coerceAtMost(1f))
}
