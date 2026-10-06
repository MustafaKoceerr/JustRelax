package com.mustafakoceerr.justrelax.service

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.test.core.app.ApplicationProvider
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    private val audioManager: AudioManager
        get() = ApplicationProvider.getApplicationContext<Context>().getSystemService(AudioManager::class.java)

    private fun startPlaying() {
        mixer.setState(GlobalMixerState(isPlaying = true, activeSounds = listOf(SoundConfig("rain", "/rain.mp3"))))
        idle()
    }

    private fun focusChange(change: Int) {
        shadowOf(audioManager).lastAudioFocusRequest.listener.onAudioFocusChange(change)
        idle()
    }

    @Test
    fun playingMix_requestsAudioFocus() {
        serviceController.create().startCommand(0, 1)

        startPlaying()

        assertNotNull(shadowOf(audioManager).lastAudioFocusRequest)
    }

    @Test
    fun phoneCall_pausesTheMix_andResumesWhenItEnds() {
        serviceController.create().startCommand(0, 1)
        startPlaying()

        focusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        assertFalse(mixer.state.value.isPlaying)

        focusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertTrue(mixer.state.value.isPlaying)
    }

    @Test
    fun anotherAppPlaying_pausesTheMixForGood() {
        serviceController.create().startCommand(0, 1)
        startPlaying()

        focusChange(AudioManager.AUDIOFOCUS_LOSS)
        focusChange(AudioManager.AUDIOFOCUS_GAIN)

        assertFalse(mixer.state.value.isPlaying)
    }

    @Test
    fun headphonesUnplugged_pausesTheMix() {
        val service = serviceController.create().startCommand(0, 1).get()
        startPlaying()

        service.sendBroadcast(Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        idle()

        assertFalse(mixer.state.value.isPlaying)
    }

    /** A Media3 controller connected like system UI / Bluetooth would be. */
    private fun connectController(service: PlaybackService): MediaController {
        val future = MediaController.Builder(service, service.sessions.single().token)
            .setApplicationLooper(Looper.getMainLooper())
            .buildAsync()
        idle()
        return future.get()
    }

    @Test
    fun controllers_areOfferedAStopButton() {
        val service = serviceController.create().startCommand(0, 1).get()
        startPlaying()

        val controller = connectController(service)

        val stopButton = controller.mediaButtonPreferences.single()
        assertEquals(PlaybackService.ACTION_STOP, stopButton.sessionCommand?.customAction)
        assertTrue(controller.isSessionCommandAvailable(stopButton.sessionCommand!!))
        controller.release()
    }

    @Test
    fun stopButton_stopsTheMix() {
        val service = serviceController.create().startCommand(0, 1).get()
        startPlaying()
        val controller = connectController(service)

        controller.sendCustomCommand(SessionCommand(PlaybackService.ACTION_STOP, Bundle.EMPTY), Bundle.EMPTY)
        idle()

        assertTrue(mixer.state.value.activeSounds.isEmpty())
        controller.release()
    }
}
