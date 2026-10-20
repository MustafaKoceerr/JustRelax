package com.mustafakoceerr.justrelax

import android.app.Application
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * Android 13+ draws a themed icon only when the adaptive icon has a monochrome layer; without it
 * the launcher shows the full-colour icon among the tinted ones.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LauncherIconTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun launcherIconHasMonochromeLayer() {
        assertMonochrome(R.mipmap.ic_launcher)
    }

    @Test
    fun roundLauncherIconHasMonochromeLayer() {
        assertMonochrome(R.mipmap.ic_launcher_round)
    }

    private fun assertMonochrome(id: Int) {
        val icon = assertIs<AdaptiveIconDrawable>(ContextCompat.getDrawable(context, id))
        assertNotNull(icon.monochrome, "The adaptive icon has no monochrome layer")
    }
}
