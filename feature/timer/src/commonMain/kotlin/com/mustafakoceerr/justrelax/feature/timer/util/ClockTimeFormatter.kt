package com.mustafakoceerr.justrelax.feature.timer.util

import androidx.compose.runtime.Composable
import kotlin.time.Instant

/** Formats a time of day in the device's 12/24-hour style. The formatter is created once. */
@Composable
internal expect fun rememberClockTimeFormatter(): (Instant) -> String
