package com.mustafakoceerr.justrelax.feature.player

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.TogglePauseResumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSavedMixRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerContract
import justrelax.feature.player.generated.resources.Res
import justrelax.feature.player.generated.resources.err_mix_save_name_exists
import justrelax.feature.player.generated.resources.msg_mix_saved_success
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerViewModelTest {

    private val mixer = FakeAudioMixer().apply {
        setState(GlobalMixerState(isPlaying = true, activeSounds = listOf(SoundConfig("rain", "/rain.mp3"))))
    }
    private val savedMixes = FakeSavedMixRepository()
    private val getMixerState = GetGlobalMixerStateUseCase(mixer)

    private fun viewModel() = PlayerViewModel(
        soundRepository = FakeSoundRepository(listOf(testSound("rain"), testSound("fire"))),
        getGlobalMixerStateUseCase = getMixerState,
        togglePauseResumeUseCase = TogglePauseResumeUseCase(mixer),
        stopAllSoundsUseCase = StopAllSoundsUseCase(mixer),
        saveCurrentMixUseCase = SaveCurrentMixUseCase(savedMixes, getMixerState),
    )

    private fun snackbarText(effect: PlayerContract.Effect) =
        ((effect as PlayerContract.Effect.ShowSnackbar).message as UiText.Resource).resId

    @Test
    fun stateShowsTheSoundsCurrentlyPlaying() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("rain"), viewModel.state.value.activeSounds.map { it.id })
        assertTrue(viewModel.state.value.isVisible)
    }

    @Test
    fun savingMix_closesDialog_andConfirms() = runMainTest {
        val viewModel = viewModel()
        viewModel.onEvent(PlayerContract.Event.OpenSaveDialog)

        viewModel.effect.test {
            viewModel.onEvent(PlayerContract.Event.SaveMix("Night"))

            assertEquals(Res.string.msg_mix_saved_success, snackbarText(awaitItem()))
        }
        assertFalse(viewModel.state.value.isSaveDialogVisible)
        assertEquals(1, savedMixes.mixes.value.size)
    }

    @Test
    fun duplicateName_keepsDialogOpen_andExplains() = runMainTest {
        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onEvent(PlayerContract.Event.SaveMix("Night"))
            assertEquals(Res.string.msg_mix_saved_success, snackbarText(awaitItem()))

            viewModel.onEvent(PlayerContract.Event.OpenSaveDialog)
            viewModel.onEvent(PlayerContract.Event.SaveMix("night"))
            assertEquals(Res.string.err_mix_save_name_exists, snackbarText(awaitItem()))
        }
        assertTrue(viewModel.state.value.isSaveDialogVisible)
        assertEquals(1, savedMixes.mixes.value.size)
    }

    @Test
    fun stopAll_stopsTheMixer() = runMainTest {
        val viewModel = viewModel()

        viewModel.onEvent(PlayerContract.Event.StopAll)
        advanceUntilIdle()

        assertTrue(mixer.state.value.activeSounds.isEmpty())
    }
}
