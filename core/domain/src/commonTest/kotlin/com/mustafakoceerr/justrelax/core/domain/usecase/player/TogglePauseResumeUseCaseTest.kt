package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TogglePauseResumeUseCaseTest {

    private val mixer = FakeAudioMixer().apply {
        setState(GlobalMixerState(isPlaying = true, activeSounds = listOf(SoundConfig("rain", "/rain.mp3"))))
    }
    private val useCase = TogglePauseResumeUseCase(mixer)

    @Test
    fun playing_isPaused_thenResumed() = runTest {
        useCase()
        assertFalse(mixer.state.value.isPlaying)

        useCase()
        assertTrue(mixer.state.value.isPlaying)
    }
}
