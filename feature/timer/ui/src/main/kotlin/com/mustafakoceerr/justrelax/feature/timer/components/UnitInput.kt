package com.mustafakoceerr.justrelax.feature.timer.components

/** What a typed hour, minute or second field keeps: up to two digits. */
internal fun sanitizeUnitInput(raw: String): String = raw.filter(Char::isDigit).take(2)

/** The typed value capped at [max]; null when nothing was typed, so the wheel keeps its value. */
internal fun parseUnitInput(text: String, max: Int): Int? = text.toIntOrNull()?.coerceAtMost(max)
