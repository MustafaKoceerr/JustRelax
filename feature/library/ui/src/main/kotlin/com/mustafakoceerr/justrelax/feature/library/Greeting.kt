package com.mustafakoceerr.justrelax.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mustafakoceerr.justrelax.feature.library.ui.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.res.stringResource
import kotlin.time.Clock

/** "Good evening" and the like, for the hour the screen was opened. */
@Composable
internal fun rememberGreeting(): String {
    val timeOfDay = remember { timeOfDayAt(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour) }
    return stringResource(
        when (timeOfDay) {
            TimeOfDay.MORNING -> R.string.greeting_morning
            TimeOfDay.AFTERNOON -> R.string.greeting_afternoon
            TimeOfDay.EVENING -> R.string.greeting_evening
            TimeOfDay.NIGHT -> R.string.greeting_night
        }
    )
}
