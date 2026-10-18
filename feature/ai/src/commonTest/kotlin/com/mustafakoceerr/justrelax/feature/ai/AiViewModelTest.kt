package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepository
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.localized
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
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
    private val fire = testSound("fire", localPath = "/sounds/fire.mp3", kind = SoundKind.EVENT)

    private inner class FakeAiMixRepository : AiMixRepository {
        var result: Resource<AiGeneratedMix> = Resource.Success(
            AiGeneratedMix(
                name = "Rainy Night",
                description = "Soft rain and a fire",
                scene = Scene(listOf(Layer.Bed("rain", 0.6f), Layer.Event("fire", 0.3f, EventFrequency.OFTEN))),
                sounds = listOf(rain.localized("en"), fire.localized("en")),
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
    private val scene = InMemorySceneRepository(repository)
    private val playback = FakePlaybackController()
    private val analytics = TestAnalyticsHelper()

    private fun TestScope.viewModel() = AiViewModel(
        aiMixRepository = ai,
        sceneRepository = scene,
        playbackController = playback,
        analytics = analytics,
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

    @Test
    fun theGeneratedScene_keepsItsEventFrequencies_whichCanBeChanged() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("campfire")
        viewModel.generateMix()
        advanceUntilIdle()
        assertEquals(mapOf("fire" to EventFrequency.OFTEN), viewModel.uiState.value.soundFrequencies)

        viewModel.changeFrequency("fire", EventFrequency.RARE)
        advanceUntilIdle()

        assertEquals(mapOf("fire" to EventFrequency.RARE), viewModel.uiState.value.soundFrequencies)
    }

    @Test
    fun aRequestAndAFailureAreCounted_withoutTheUsersWords() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("a secret rainy night")

        viewModel.generateMix()
        advanceUntilIdle()
        ai.result = Resource.Error(AppError.Network.RateLimited())
        viewModel.generateMix()
        advanceUntilIdle()

        assertEquals(
            listOf(
                AnalyticsEvent(EventTypes.AI_MIX_REQUESTED),
                AnalyticsEvent(EventTypes.AI_MIX_REQUESTED),
                AnalyticsEvent(EventTypes.AI_MIX_FAILED, mapOf("error" to "RateLimited")),
            ),
            analytics.events,
        )
        assertTrue(analytics.events.none { event -> event.params.values.any { "secret" in it } })
    }
}
