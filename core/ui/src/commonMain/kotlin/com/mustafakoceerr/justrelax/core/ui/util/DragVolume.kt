package com.mustafakoceerr.justrelax.core.ui.util

/**
 * The volume after a horizontal drag of [dragX] pixels across something [width] pixels wide, as a
 * sound tile does: a drag across the whole width goes from silent to full.
 */
fun volumeAfterDrag(start: Float, dragX: Float, width: Float): Float {
    if (width <= 0f) return start
    return (start + dragX / width).coerceIn(0f, 1f)
}
