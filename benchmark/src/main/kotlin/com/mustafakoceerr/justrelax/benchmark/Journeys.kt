package com.mustafakoceerr.justrelax.benchmark

import android.graphics.Point
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

/** The benchmark builds' application id (androidApp adds `.benchmark`), so they install beside the Play app. */
const val PACKAGE_NAME = "com.mustafakoceerr.justrelax.benchmark"

/** A copy of core:ui's TestTags; the benchmark module does not depend on app code. */
object Tags {
    const val CONSENT_DECLINE = "consent_decline"
    const val SOUND_GRID = "sound_grid"
    const val SOUND_CARD = "sound_card"
    const val VOLUME_SLIDER = "volume_slider"
    const val TIMER_WHEEL_MINUTE = "timer_wheel_minute"
    const val TIMER_START = "timer_start"
    const val TIMER_CANCEL = "timer_cancel"
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

/** The Timer tab in setup mode; a timer left running by an earlier run is cancelled. */
fun MacrobenchmarkScope.openTimerSetup(): UiObject2 {
    openTab("timer")
    findOrNull(Tags.TIMER_CANCEL)?.click()
    return waitFor(Tags.TIMER_WHEEL_MINUTE)
}

fun MacrobenchmarkScope.flingTwice(view: UiObject2) {
    // Keep gestures away from the screen edges, where they become system back or home gestures.
    view.setGestureMargin(device.displayWidth / 10)
    view.fling(Direction.DOWN)
    device.waitForIdle()
    view.fling(Direction.UP)
    device.waitForIdle()
}

fun MacrobenchmarkScope.scrollSoundGrid() {
    val grid = waitFor(Tags.SOUND_GRID)
    grid.setGestureMargin(device.displayWidth / 10)
    grid.fling(Direction.DOWN)
    device.waitForIdle()
    grid.fling(Direction.UP)
    device.waitForIdle()
}

/** Drags [slider] from one end to the other and back. */
fun MacrobenchmarkScope.dragAcross(slider: UiObject2) {
    val bounds = slider.visibleBounds
    val y = bounds.centerY()
    slider.drag(Point(bounds.left + bounds.width() / 10, y), 600)
    slider.drag(Point(bounds.right - bounds.width() / 10, y), 600)
    device.waitForIdle()
}
