package com.mustafakoceerr.justrelax.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mustafakoceerr.justrelax.feature.library.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.library.generated.resources.greeting_afternoon
import com.mustafakoceerr.justrelax.feature.library.generated.resources.greeting_evening
import com.mustafakoceerr.justrelax.feature.library.generated.resources.greeting_morning
import com.mustafakoceerr.justrelax.feature.library.generated.resources.greeting_night
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/** Which part of the day it is, for the greeting on top of the library. */
enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

/** [hour] is 0–23 in the device's time zone. */
fun timeOfDayAt(hour: Int): TimeOfDay = when (hour) {
    in 5..11 -> TimeOfDay.MORNING
    in 12..16 -> TimeOfDay.AFTERNOON
    in 17..21 -> TimeOfDay.EVENING
    else -> TimeOfDay.NIGHT
}

/** "Good evening" and the like, for the hour the screen was opened. */
@Composable
internal fun rememberGreeting(): String {
    val timeOfDay = remember { timeOfDayAt(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour) }
    return stringResource(
        when (timeOfDay) {
            TimeOfDay.MORNING -> Res.string.greeting_morning
            TimeOfDay.AFTERNOON -> Res.string.greeting_afternoon
            TimeOfDay.EVENING -> Res.string.greeting_evening
            TimeOfDay.NIGHT -> Res.string.greeting_night
        }
    )
}
