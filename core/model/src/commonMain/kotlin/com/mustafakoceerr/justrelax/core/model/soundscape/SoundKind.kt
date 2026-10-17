package com.mustafakoceerr.justrelax.core.model.soundscape

/**
 * How a sound is played in a soundscape:
 * - [BED]: a short seamless loop that runs continuously (rain, wind, a creek, noise).
 * - [EVENT]: short clips played at random intervals, volumes and stereo positions
 *   (a bird call, an owl, a crackling log, thunder), so the scene never repeats exactly.
 */
enum class SoundKind { BED, EVENT }
