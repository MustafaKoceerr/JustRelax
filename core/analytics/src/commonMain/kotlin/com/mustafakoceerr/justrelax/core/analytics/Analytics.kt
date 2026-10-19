package com.mustafakoceerr.justrelax.core.analytics

/**
 * Something the user did, for usage analytics. Parameters never carry what the user typed (AI
 * words, search text), file names or ids that could identify a person.
 */
data class AnalyticsEvent(val type: String, val params: Map<String, String> = emptyMap())

/** Sends [AnalyticsEvent]s; does nothing until the user has agreed ([setEnabled]). */
interface AnalyticsHelper {
    fun logEvent(event: AnalyticsEvent)
    fun setEnabled(enabled: Boolean)
}

/** Reports crashes and caught errors worth knowing about; off until the user has agreed. */
interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun setEnabled(enabled: Boolean)
}

object NoOpAnalyticsHelper : AnalyticsHelper {
    override fun logEvent(event: AnalyticsEvent) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
}

object NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
}

fun AnalyticsHelper.logScreenView(screenName: String) =
    logEvent(AnalyticsEvent(EventTypes.SCREEN_VIEW, mapOf(Params.SCREEN_NAME to screenName)))

/** The sleep timer was started, from its screen or the scene panel, with a rough length of [seconds]. */
fun AnalyticsHelper.logTimerStarted(seconds: Long) =
    logEvent(AnalyticsEvent(EventTypes.TIMER_STARTED, mapOf(Params.DURATION_BUCKET to timerDurationBucket(seconds))))

/** A rough length, enough to see how people use the timer. */
private fun timerDurationBucket(seconds: Long): String = when {
    seconds < 15 * 60 -> "under_15m"
    seconds < 30 * 60 -> "15_30m"
    seconds <= 60 * 60 -> "30_60m"
    seconds <= 2 * 60 * 60 -> "1_2h"
    else -> "over_2h"
}

/** Event and parameter names, kept in one place so the dashboards stay consistent. */
object EventTypes {
    const val SCREEN_VIEW = "screen_view"
    const val SOUND_PLAY = "sound_play"
    const val MIX_SAVED = "mix_saved"
    const val AI_MIX_REQUESTED = "ai_mix_requested"
    const val AI_MIX_FAILED = "ai_mix_failed"
    const val FREESOUND_SEARCH = "freesound_search"
    const val SOUND_IMPORTED = "sound_imported"
    const val TIMER_STARTED = "timer_started"
}

object Params {
    const val SCREEN_NAME = "screen_name"
    const val SOUND_ID = "sound_id"
    const val CATEGORY = "category"
    const val SOURCE = "source"
    const val KIND = "kind"
    const val LAYER_COUNT = "layer_count"
    const val RESULT_COUNT = "result_count"
    const val ERROR = "error"
    const val DURATION_BUCKET = "duration_bucket"
}
