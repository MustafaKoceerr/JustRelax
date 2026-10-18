package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.playback.di.playbackModule
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertFalse

/** The iOS graph builds: the audio session, lock screen controls and players come up without a device. */
class IosPlaybackModuleTest {

    @Test
    fun thePlaybackController_resolvesAndAnswersPlayAndPause() {
        val sounds = FakeSoundRepository()
        val koin = koinApplication {
            modules(
                module {
                    single<SoundRepository> { sounds }
                    single<SceneRepository> { InMemorySceneRepository(sounds) }
                },
                playbackModule,
            )
        }.koin

        val controller = koin.get<PlaybackController>()
        controller.play()
        controller.pause()

        assertFalse(controller.isPlaying.value)
        koin.close()
    }
}
