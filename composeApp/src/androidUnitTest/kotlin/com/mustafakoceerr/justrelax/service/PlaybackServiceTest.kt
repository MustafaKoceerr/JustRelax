package com.mustafakoceerr.justrelax.service

import android.app.Application
import android.os.Looper
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The service is started with startService (no MediaController binds to it), so Media3 must
 * still manage the session's notification and promote the service to the foreground; otherwise
 * Android kills it shortly after the app goes to the background.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PlaybackServiceTest {

    private val mixer = FakeAudioMixer()

    @BeforeTest
    fun setUp() {
        startKoin { modules(module { single<AudioMixer> { mixer } }) }
    }

    private val serviceController = Robolectric.buildService(PlaybackService::class.java)

    @AfterTest
    fun tearDown() {
        serviceController.destroy() // releases the MediaSession (its id is registered process-wide)
        stopKoin()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun sessionIsRegisteredWithTheService_withoutAControllerBinding() {
        val service = serviceController.create().startCommand(0, 1).get()

        assertEquals(1, service.sessions.size)
    }

    @Test
    fun playingMix_promotesServiceToForegroundWithNotification() {
        val service = serviceController.create().startCommand(0, 1).get()

        mixer.setState(GlobalMixerState(isPlaying = true, activeSounds = listOf(SoundConfig("rain", "/rain.mp3"))))
        idle()
        Thread.sleep(500) // Media3 builds the notification (bitmap loading) off the main thread
        idle()

        assertNotNull(shadowOf(service).lastForegroundNotification)
    }
}
