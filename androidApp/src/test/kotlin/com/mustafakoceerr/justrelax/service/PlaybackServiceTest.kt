package com.mustafakoceerr.justrelax.service

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import androidx.test.core.app.ApplicationProvider
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.playback.di.playbackModule
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSleepTimer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundPlayer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The real service, SoundscapePlayer and Media3 session, driven through a MediaController the way
 * the app and system UI do; only the audio output is faked.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PlaybackServiceTest {

    private val beds = mutableListOf<FakeSoundPlayer>()
    private val sounds = FakeSoundRepository(listOf(testSound("rain", localPath = "/sounds/rain.mp3")))
    private val scene = InMemorySceneRepository(sounds)
    private val timer = FakeSleepTimer()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val audioManager: AudioManager get() = context.getSystemService(AudioManager::class.java)

    @BeforeTest
    fun setUp() {
        startKoin {
            allowOverride(true)
            androidContext(context)
            modules(
                playbackModule,
                module {
                    single<SoundRepository> { sounds }
                    single<SceneRepository> { scene }
                    single<SleepTimer> { timer }
                    single<SoundPlayerFactory> { SoundPlayerFactory { FakeSoundPlayer().also { beds += it } } }
                },
            )
        }
    }

    private val serviceController = Robolectric.buildService(PlaybackService::class.java)
    private lateinit var controller: MediaController

    @AfterTest
    fun tearDown() {
        if (::controller.isInitialized) controller.release()
        serviceController.destroy() // releases the MediaSession (its id is registered process-wide)
        stopKoin()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    /** Connects a MediaController the way the app does: through the service's component name. */
    private fun start(): PlaybackService {
        val service = serviceController.create().get()
        val component = ComponentName(context, PlaybackService::class.java)
        shadowOf(context as Application).setComponentNameAndServiceForBindService(
            component,
            service.onBind(Intent(MediaSessionService.SERVICE_INTERFACE)),
        )
        val future = MediaController.Builder(context, SessionToken(context, component))
            .setApplicationLooper(Looper.getMainLooper())
            .buildAsync()
        idle()
        controller = future.get()
        return service
    }

    private fun addRain() {
        runBlocking { scene.toggle("rain") }
        idle()
    }

    private fun startPlaying(): PlaybackService = start().also {
        addRain()
        controller.play()
        idle()
    }

    private val isSounding get() = beds.any { it.isPlaying }

    private fun focusChange(change: Int) {
        shadowOf(audioManager).lastAudioFocusRequest.listener.onAudioFocusChange(change)
        idle()
    }

    // --- What the session shows

    @Test
    fun anEmptyScene_isIdle() {
        start()

        assertEquals(Player.STATE_IDLE, controller.playbackState)
        assertEquals(0, controller.mediaItemCount)
    }

    @Test
    fun aScene_isOneReadyItem_withTheMixMetadata_andWaitsForPlay() {
        start()

        addRain()

        assertEquals(Player.STATE_READY, controller.playbackState)
        assertEquals(1, controller.mediaItemCount)
        assertNotNull(controller.mediaMetadata.title)
        assertFalse(controller.playWhenReady)
        assertFalse(isSounding)
    }

    @Test
    fun play_startsTheSounds_andPromotesTheServiceToTheForeground() {
        val service = startPlaying()
        Thread.sleep(500) // Media3 builds the notification (bitmap loading) off the main thread
        idle()

        assertTrue(controller.isPlaying)
        assertTrue(isSounding)
        assertNotNull(shadowOf(service).lastForegroundNotification)
    }

    @Test
    fun pause_fromAController_silencesTheScene() {
        startPlaying()

        controller.pause()
        idle()

        assertFalse(isSounding)
        assertEquals(listOf("rain"), scene.scene.value.layers.map { it.soundId })
    }

    // --- Interruptions (Media3's audio focus handling)

    @Test
    fun playing_requestsAudioFocus() {
        startPlaying()

        assertNotNull(shadowOf(audioManager).lastAudioFocusRequest)
    }

    @Test
    fun aPhoneCall_pauses_andPlaybackResumesWhenItEnds() {
        startPlaying()

        focusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        assertFalse(isSounding)
        assertTrue(controller.playWhenReady)
        assertEquals(Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS, controller.playbackSuppressionReason)

        focusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertTrue(isSounding)
    }

    @Test
    fun anotherAppPlaying_pausesForGood() {
        startPlaying()

        focusChange(AudioManager.AUDIOFOCUS_LOSS)

        assertFalse(isSounding)
        assertFalse(controller.playWhenReady)
    }

    @Test
    fun pausingDuringACall_winsOverResuming() {
        startPlaying()
        focusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)

        controller.pause()
        idle()
        focusChange(AudioManager.AUDIOFOCUS_GAIN)

        assertFalse(isSounding)
    }

    @Test
    fun aShortSound_ducksTheScene() {
        startPlaying()
        val fullVolume = beds.single().volume!!

        focusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)

        assertTrue(isSounding)
        assertTrue(beds.single().volume!! < fullVolume)
    }

    @Test
    fun headphonesUnplugged_pauses() {
        val service = startPlaying()

        service.sendBroadcast(Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        idle()

        assertFalse(isSounding)
        assertFalse(controller.playWhenReady)
    }

    // --- Stop and the sleep timer

    @Test
    fun controllers_areOfferedAStopButton() {
        start()

        val stopButton = controller.mediaButtonPreferences.single()
        assertEquals(PlaybackService.ACTION_STOP, stopButton.sessionCommand?.customAction)
        assertTrue(controller.isSessionCommandAvailable(stopButton.sessionCommand!!))
    }

    @Test
    fun theStopButton_clearsTheScene() {
        startPlaying()

        controller.sendCustomCommand(SessionCommand(PlaybackService.ACTION_STOP, Bundle.EMPTY), Bundle.EMPTY)
        idle()

        assertTrue(scene.scene.value.layers.isEmpty())
        assertFalse(isSounding)
        assertEquals(Player.STATE_IDLE, controller.playbackState)
    }

    @Test
    fun anEmptiedScene_stopsTheServiceMedia3Started() {
        val service = startPlaying()

        controller.sendCustomCommand(SessionCommand(PlaybackService.ACTION_STOP, Bundle.EMPTY), Bundle.EMPTY)
        idle()

        assertTrue(shadowOf(service).isStoppedBySelf)
    }

    @Test
    fun theSleepTimer_fadesOut_andPauses() {
        startPlaying()

        timer.expire()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(11))

        assertFalse(isSounding)
        assertFalse(controller.playWhenReady)
        assertEquals(listOf("rain"), scene.scene.value.layers.map { it.soundId })
    }
}
