package com.mustafakoceerr.justrelax.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Frame times on the sleep timer: spinning a wheel, and the countdown screen while it runs. */
@RunWith(AndroidJUnit4::class)
class TimerBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun flingMinuteWheel() = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            startApp()
            openTimerSetup()
        },
    ) {
        flingTwice(waitFor(Tags.TIMER_WHEEL_MINUTE))
    }

    @Test
    fun runningCountdown() = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            startApp()
            // Any non-zero time enables Start.
            flingTwice(openTimerSetup())
            waitFor(Tags.TIMER_START).click()
            waitFor(Tags.TIMER_CANCEL)
        },
    ) {
        Thread.sleep(COUNTDOWN_MS)
        waitFor(Tags.TIMER_CANCEL).click()
        device.waitForIdle()
    }

    private companion object {
        const val COUNTDOWN_MS = 5_000L
    }
}
