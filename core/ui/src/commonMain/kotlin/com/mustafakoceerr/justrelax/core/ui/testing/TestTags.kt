package com.mustafakoceerr.justrelax.core.ui.testing

/**
 * Test tags the benchmarks (`:benchmark`) find views by. On Android they become resource ids
 * (`testTagsAsResourceId` in MainActivity), so they work in every language. Keep them in sync with
 * the copy in the benchmark module.
 */
object TestTags {
    const val CONSENT_ACCEPT = "consent_accept"
    const val CONSENT_DECLINE = "consent_decline"

    const val CATEGORY_TAB = "category_tab"
    const val SOUND_GRID = "sound_grid"
    const val SOUND_CARD = "sound_card"
    const val VOLUME_SLIDER = "volume_slider"

    const val TIMER_WHEEL_HOUR = "timer_wheel_hour"
    const val TIMER_WHEEL_MINUTE = "timer_wheel_minute"
    const val TIMER_WHEEL_SECOND = "timer_wheel_second"
    const val TIMER_START = "timer_start"
    const val TIMER_CANCEL = "timer_cancel"

    /** `tab_home`, `tab_timer`, … */
    fun tab(name: String): String = "tab_${name.lowercase()}"
}
