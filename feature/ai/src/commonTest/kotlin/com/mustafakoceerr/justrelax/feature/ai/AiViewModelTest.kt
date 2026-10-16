package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.playback.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.playback.usecase.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.playback.usecase.SetMixUseCase
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepository
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.localized
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundscapeEngine
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.err_ai_empty_response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiViewModelTest {

    private val rain = testSound("rain", localPath = "/sounds/rain.mp3")
    private val fire = testSound("fire", localPath = "/sounds/fire.mp3")

    private inner class FakeAiMixRepository : AiMixRepository {
        var result: Resource<AiGeneratedMix> = Resource.Success(
            AiGeneratedMix(
                name = "Rainy Night",
                description = "Soft rain and a fire",
                sounds = mapOf(rain.localized("en") to 0.6f, fire.localized("en") to 0.3f),
            )
        )
        var lastPrompt: String? = null

        override fun generateMix(prompt: String): Flow<Resource<AiGeneratedMix>> = flow {
            lastPrompt = prompt
            emit(Resource.Loading)
            emit(result)
        }
    }

    private val ai = FakeAiMixRepository()
    private val repository = FakeSoundRepository(listOf(rain, fire))
    private val mixer = FakeSoundscapeEngine()

    private fun TestScope.viewModel() = AiViewModel(
        aiMixRepository = ai,
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
        assertEquals("rainy evening by the fire", ai.lastPrompt)
    }

    @Test
    fun aiReturningNoUsableSounds_explainsWhy() = runMainTest {
        ai.result = Resource.Error(AppError.Ai.EmptyResponse())
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
        ai.result = Resource.Error(AppError.Ai.ApiError(500, "down"))
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
