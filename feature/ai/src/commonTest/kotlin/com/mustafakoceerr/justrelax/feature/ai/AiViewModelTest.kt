package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.audio.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiMixResponse
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiMixSound
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.GenerateAiMixUseCase
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.err_ai_empty_response
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiViewModelTest {

    private class FakeAiRepository : AiRepository {
        var response: Resource<AiMixResponse> = Resource.Success(
            AiMixResponse(
                mixName = "Rainy Night",
                description = "Soft rain and a fire",
                sounds = listOf(AiMixSound("rain", 0.6f), AiMixSound("fire", 0.3f), AiMixSound("unknown", 0.5f)),
            )
        )
        var lastPrompt: String? = null

        override suspend fun generateMix(prompt: String, availableSoundIds: List<String>): Resource<AiMixResponse> {
            lastPrompt = prompt
            return response
        }
    }

    private val ai = FakeAiRepository()
    private val repository = FakeSoundRepository(
        listOf(testSound("rain", localPath = "/sounds/rain.mp3"), testSound("fire", localPath = "/sounds/fire.mp3"))
    )
    private val mixer = FakeAudioMixer()

    private fun TestScope.viewModel() = AiViewModel(
        generateAiMixUseCase = GenerateAiMixUseCase(ai, repository, FakeLanguageController()),
        setMixUseCase = SetMixUseCase(mixer),
        soundController = SoundControllerImpl(mixer, PlaySoundUseCase(repository, mixer)),
    ).also { observe(it.uiState) }

    @Test
    fun blankPrompt_doesNothing() = runMainTest {
        val viewModel = viewModel()

        viewModel.generateMix()
        advanceUntilIdle()

        assertEquals(null, ai.lastPrompt)
    }

    @Test
    fun generatingAMix_playsTheKnownSounds_andShowsTheResult() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rainy evening by the fire")

        viewModel.generateMix()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Rainy Night", state.mixName)
        assertEquals(setOf("rain", "fire"), state.mixSounds.map { it.id }.toSet())
        assertEquals(setOf("rain", "fire"), state.playingSoundIds)
        assertEquals(0.6f, state.soundVolumes["rain"])
        assertTrue(ai.lastPrompt!!.startsWith("rainy evening by the fire"))
    }

    @Test
    fun aiReturningNoUsableSounds_explainsWhy() = runMainTest {
        ai.response = Resource.Success(AiMixResponse("x", "y", listOf(AiMixSound("unknown", 0.5f))))
        val viewModel = viewModel()
        viewModel.updatePrompt("anything")

        viewModel.generateMix()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.mixSounds.isEmpty())
        assertEquals(Res.string.err_ai_empty_response, (state.userMessage as UiText.Resource).resId)
    }

    @Test
    fun aiError_stopsLoading_andShowsAMessage() = runMainTest {
        ai.response = Resource.Error(AppError.Ai.ApiError(500, "down"))
        val viewModel = viewModel()
        viewModel.updatePrompt("anything")

        viewModel.generateMix()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.userMessage != null)
    }

    @Test
    fun editPrompt_keepsThePrompt_butClearsTheResult() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")
        viewModel.generateMix()
        advanceUntilIdle()

        viewModel.editPrompt()
        advanceUntilIdle()

        assertEquals("rain", viewModel.uiState.value.prompt)
        assertTrue(viewModel.uiState.value.mixSounds.isEmpty())
    }

    @Test
    fun clearMix_resetsEverything() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")
        viewModel.generateMix()
        advanceUntilIdle()

        viewModel.clearMix()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.prompt)
        assertTrue(viewModel.uiState.value.mixSounds.isEmpty())
    }
}
