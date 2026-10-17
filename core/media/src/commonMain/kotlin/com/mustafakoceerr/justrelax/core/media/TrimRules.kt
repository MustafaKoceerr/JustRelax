package com.mustafakoceerr.justrelax.core.media

import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind

/** The part of a source file a user sound keeps, in milliseconds. */
data class TrimRange(val startMs: Long, val endMs: Long) {
    val lengthMs: Long get() = endMs - startMs
}

enum class TrimProblem { OUTSIDE_FILE, TOO_SHORT, TOO_SHORT_FOR_BED, TOO_LONG_FOR_EVENT }

/**
 * How long a user sound may be. An event is held in memory by SoundPool, so it stays short; a bed
 * loops, so it needs enough length not to sound repetitive.
 */
object TrimRules {
    const val MIN_MS = 300L
    const val MIN_BED_MS = 5_000L
    const val MAX_EVENT_MS = 10_000L

    /** Files shorter than this start as events, as most short recordings are one-off sounds. */
    private const val BED_BY_DEFAULT_FROM_MS = 15_000L

    fun defaultKind(sourceMs: Long): SoundKind =
        if (sourceMs >= BED_BY_DEFAULT_FROM_MS) SoundKind.BED else SoundKind.EVENT

    fun allowedKinds(sourceMs: Long): Set<SoundKind> =
        if (sourceMs >= MIN_BED_MS) setOf(SoundKind.BED, SoundKind.EVENT) else setOf(SoundKind.EVENT)

    fun defaultRange(kind: SoundKind, sourceMs: Long): TrimRange = fit(TrimRange(0, sourceMs), kind, sourceMs)

    /** The start handle dragged to [toMs]; it stops at the file's start and at the kind's limits. */
    fun moveStart(range: TrimRange, toMs: Long, kind: SoundKind, sourceMs: Long): TrimRange {
        val earliest = maxOf(0L, range.endMs - maxLength(kind, sourceMs))
        val latest = range.endMs - minLength(kind)
        return range.copy(startMs = toMs.coerceIn(earliest, maxOf(earliest, latest)))
    }

    /** The end handle dragged to [toMs]; it stops at the file's end and at the kind's limits. */
    fun moveEnd(range: TrimRange, toMs: Long, kind: SoundKind, sourceMs: Long): TrimRange {
        val earliest = range.startMs + minLength(kind)
        val latest = minOf(sourceMs, range.startMs + maxLength(kind, sourceMs))
        return range.copy(endMs = toMs.coerceIn(minOf(earliest, latest), latest))
    }

    /** [range] changed as little as possible to suit [kind], e.g. after the kind was switched. */
    fun fit(range: TrimRange, kind: SoundKind, sourceMs: Long): TrimRange {
        val start = range.startMs.coerceIn(0L, sourceMs)
        var end = range.endMs.coerceIn(start, sourceMs)
        end = end.coerceIn(minOf(sourceMs, start + minLength(kind)), start + maxLength(kind, sourceMs))
        return TrimRange(maxOf(0L, minOf(start, end - minLength(kind))), end)
    }

    fun problem(range: TrimRange, kind: SoundKind, sourceMs: Long): TrimProblem? = when {
        range.startMs < 0 || range.endMs > sourceMs -> TrimProblem.OUTSIDE_FILE
        range.lengthMs < MIN_MS -> TrimProblem.TOO_SHORT
        kind == SoundKind.BED && range.lengthMs < MIN_BED_MS -> TrimProblem.TOO_SHORT_FOR_BED
        kind == SoundKind.EVENT && range.lengthMs > MAX_EVENT_MS -> TrimProblem.TOO_LONG_FOR_EVENT
        else -> null
    }

    private fun minLength(kind: SoundKind) = if (kind == SoundKind.BED) MIN_BED_MS else MIN_MS

    private fun maxLength(kind: SoundKind, sourceMs: Long) = if (kind == SoundKind.EVENT) MAX_EVENT_MS else sourceMs
}
