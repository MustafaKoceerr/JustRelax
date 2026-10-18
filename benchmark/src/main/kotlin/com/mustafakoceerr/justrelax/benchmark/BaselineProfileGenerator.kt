package com.mustafakoceerr.justrelax.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Writes androidApp/src/release/generated/baselineProfiles/ with
 * `./gradlew :androidApp:generateReleaseBaselineProfile` (Android 13+ device, not rooted is fine).
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        startApp()
        scrollSoundGrid()

        flingTwice(openTimerSetup())

        openTab("ai")
        openTab("saved")
        openTab("mixer")
        openTab("home")
    }
}
