package com.mustafakoceerr.justrelax.feature.library

/** Which part of the day it is, for the greeting on top of the library. */
enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

/** [hour] is 0–23 in the device's time zone. */
fun timeOfDayAt(hour: Int): TimeOfDay = when (hour) {
    in 5..11 -> TimeOfDay.MORNING
    in 12..16 -> TimeOfDay.AFTERNOON
    in 17..21 -> TimeOfDay.EVENING
    else -> TimeOfDay.NIGHT
}
