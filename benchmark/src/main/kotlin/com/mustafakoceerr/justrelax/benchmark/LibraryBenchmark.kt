package com.mustafakoceerr.justrelax.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame times on the Home tab. Needs the built-in pack downloaded (one normal launch with internet
 * is enough), so the first card plays when tapped.
 */
@RunWith(AndroidJUnit4::class)
class LibraryBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollSounds() = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = { startApp() },
    ) {
        scrollSoundGrid()
    }

    /** Every drag step changes the scene; this shows how much of the screen redraws for it. */
    @Test
    fun dragVolume() = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            startApp()
            waitFor(Tags.SOUND_CARD).click()
            waitFor(Tags.VOLUME_SLIDER)
        },
    ) {
        dragAcross(waitFor(Tags.VOLUME_SLIDER))
        // Stops the sound so the next iteration starts the same way.
        waitFor(Tags.SOUND_CARD).click()
        device.waitForIdle()
    }
}
