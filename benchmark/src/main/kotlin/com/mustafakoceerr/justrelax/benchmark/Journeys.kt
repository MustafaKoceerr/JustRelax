package com.mustafakoceerr.justrelax.benchmark

import android.graphics.Point
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

/** The benchmark builds' application id (androidApp adds `.benchmark`), so they install beside the Play app. */
const val PACKAGE_NAME = "com.mustafakoceerr.justrelax.benchmark"

/** A copy of core:ui's TestTags; the benchmark module does not depend on app code. */
object Tags {
    const val CONSENT_DECLINE = "consent_decline"
    const val CATEGORY_TAB = "category_tab"
    const val SOUND_GRID = "sound_grid"
    const val SOUND_CARD = "sound_card"
    const val VOLUME_SLIDER = "volume_slider"
    const val TIMER_WHEEL_MINUTE = "timer_wheel_minute"
    const val TIMER_START = "timer_start"
    const val TIMER_CANCEL = "timer_cancel"
    const val MINI_PLAYER_TIMER = "mini_player_timer"
    fun tab(name: String) = "tab_$name"
}

private const val TIMEOUT_MS = 10_000L

fun MacrobenchmarkScope.waitFor(tag: String, timeoutMs: Long = TIMEOUT_MS): UiObject2 =
    device.wait(Until.findObject(By.res(tag)), timeoutMs)
        ?: error("No view with test tag '$tag' after $timeoutMs ms")

fun MacrobenchmarkScope.findOrNull(tag: String): UiObject2? = device.findObject(By.res(tag))

/** Starts the app and answers the first-launch consent screen if it shows. Ends on the Home tab. */
fun MacrobenchmarkScope.startApp() {
    pressHome()
    startActivityAndWait()
    skipConsentIfShown()
}

fun MacrobenchmarkScope.skipConsentIfShown() {
    // The splash decides between Consent and Main; wait until one of them is on screen.
    device.wait(Until.hasObject(By.res(Pattern.compile("${Tags.CONSENT_DECLINE}|${Tags.tab("home")}"))), TIMEOUT_MS)
    findOrNull(Tags.CONSENT_DECLINE)?.click()
    waitFor(Tags.tab("home"))
}

fun MacrobenchmarkScope.openTab(name: String) {
    waitFor(Tags.tab(name)).click()
    device.waitForIdle()
}

/**
 * The sleep timer in setup mode; a timer left running by an earlier run is cancelled. It is opened
 * from the mini player, so a sound is started first if nothing plays.
 */
fun MacrobenchmarkScope.openTimerSetup(): UiObject2 {
    if (findOrNull(Tags.MINI_PLAYER_TIMER) == null) {
        openTab("home")
        waitFor(Tags.SOUND_CARD).click()
    }
    waitFor(Tags.MINI_PLAYER_TIMER).click()
    findOrNull(Tags.TIMER_CANCEL)?.click()
    return waitFor(Tags.TIMER_WHEEL_MINUTE)
}

/**
 * Flicks [view] up and then down inside its own bounds. UiObject2.fling did not reach the timer's
 * wheel on a real phone (no frames were drawn), so the swipe is spelled out.
 */
fun MacrobenchmarkScope.flingTwice(view: UiObject2) {
    flick(view, up = true)
    flick(view, up = false)
}

/** One fast swipe up (to later numbers) or down inside [view]. */
fun MacrobenchmarkScope.flick(view: UiObject2, up: Boolean) {
    val bounds = view.visibleBounds
    val x = bounds.centerX()
    val low = bounds.top + bounds.height() * 3 / 4
    val high = bounds.top + bounds.height() / 4
    if (up) device.swipe(x, low, x, high, FLICK_STEPS) else device.swipe(x, high, x, low, FLICK_STEPS)
    device.waitForIdle()
}

/** About 5 ms per step: a 80 ms swipe, fast enough to fling and with enough points to measure its speed. */
private const val FLICK_STEPS = 16

/**
 * Moves a few categories to the right on Home and back. The built-in pack has only a handful of
 * sounds per category, too few to scroll the grid, so changing category is what redraws it.
 * The row centers the selected tab, so the last visible tab is the next one and the first the previous.
 */
fun MacrobenchmarkScope.switchCategories() {
    waitFor(Tags.SOUND_GRID)
    repeat(CATEGORY_HOPS) { selectVisibleCategory(last = true) }
    repeat(CATEGORY_HOPS) { selectVisibleCategory(last = false) }
}

private fun MacrobenchmarkScope.selectVisibleCategory(last: Boolean) {
    val tabs = device.findObjects(By.res(Tags.CATEGORY_TAB))
    if (tabs.isEmpty()) return
    (if (last) tabs.last() else tabs.first()).click()
    device.waitForIdle()
}

private const val CATEGORY_HOPS = 3

/** Drags [slider] from one end to the other and back. */
fun MacrobenchmarkScope.dragAcross(slider: UiObject2) {
    val bounds = slider.visibleBounds
    val y = bounds.centerY()
    slider.drag(Point(bounds.left + bounds.width() / 10, y), 600)
    slider.drag(Point(bounds.right - bounds.width() / 10, y), 600)
    device.waitForIdle()
}
