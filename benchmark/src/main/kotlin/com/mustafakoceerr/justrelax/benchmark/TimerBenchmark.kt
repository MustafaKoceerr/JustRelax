package com.mustafakoceerr.justrelax.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
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
            flick(openTimerSetup(), up = true)
            // Start turns on only once the wheel has come to rest on its new value.
            val start = device.wait(Until.findObject(By.res(Tags.TIMER_START).enabled(true)), 10_000)
                ?: error("Start stayed disabled after turning the wheel")
            start.click()
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
