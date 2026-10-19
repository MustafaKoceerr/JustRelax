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
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLegalRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSystemLauncher
import com.mustafakoceerr.justrelax.core.testing.fake.FakeUserPreferencesRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_busy
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_empty_response
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_offline
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_unavailable
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.err_ai_unverified
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
    // Most tests are about the mix itself, so the OpenAI notice was already accepted.
    private val preferences = FakeUserPreferencesRepository().apply { aiDataNoticeAccepted.value = true }
    private val launcher = FakeSystemLauncher()

    private fun TestScope.viewModel() = AiViewModel(
        aiMixRepository = ai,
        sceneRepository = scene,
        playbackController = playback,
        analytics = analytics,
        preferences = preferences,
        legalRepository = FakeLegalRepository(),
        systemLauncher = launcher,
    ).also {
        observe(it.uiState)
        observe(it.volumes)
    }

    @Test
    fun theFirstMix_asksAboutOpenAiFirst_andSendsNothing() = runMainTest {
        preferences.aiDataNoticeAccepted.value = false
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")

        viewModel.generateMix()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataNotice)
        assertEquals(null, ai.lastPrompt)
    }

    @Test
    fun acceptingTheNotice_remembersIt_andSendsTheMix() = runMainTest {
        preferences.aiDataNoticeAccepted.value = false
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")
        viewModel.generateMix()
        advanceUntilIdle()

        viewModel.acceptAiDataNotice()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAiDataNotice)
        assertTrue(preferences.aiDataNoticeAccepted.value)
        assertEquals("rain", ai.lastPrompt)
        assertEquals("Rainy Night", viewModel.uiState.value.mixName)
    }

    @Test
    fun onceAccepted_theNoticeIsNotShownAgain() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")

        viewModel.generateMix()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAiDataNotice)
        assertEquals("rain", ai.lastPrompt)
    }

    @Test
    fun dismissingTheNotice_sendsNothing() = runMainTest {
        preferences.aiDataNoticeAccepted.value = false
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")
        viewModel.generateMix()
        advanceUntilIdle()

        viewModel.dismissAiDataNotice()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAiDataNotice)
        assertFalse(preferences.aiDataNoticeAccepted.value)
        assertEquals(null, ai.lastPrompt)
    }

    @Test
    fun theNotice_opensThePrivacyPolicy() = runMainTest {
        val viewModel = viewModel()

        viewModel.openPrivacyPolicy()
        advanceUntilIdle()

        assertEquals(listOf("https://legal.test/en/privacy"), launcher.openedUrls)
    }

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
        assertEquals(setOf("rain", "fire"), state.sceneCards.playingIds)
        assertEquals(0.6f, viewModel.volumes.value["rain"])
        assertEquals("rainy evening by the fire", ai.lastPrompt)
    }

    @Test
    fun changingVolume_leavesTheScreenStateAlone() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rainy evening by the fire")
        viewModel.generateMix()
        advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.changeVolume("rain", 0.2f)
        advanceUntilIdle()

        assertEquals(0.2f, viewModel.volumes.value["rain"])
        assertEquals(before, viewModel.uiState.value)
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
    fun eachFailure_explainsItselfInsteadOfAGenericError() = runMainTest {
        val expected = listOf(
            AppError.Network.NoInternet() to Res.string.err_ai_offline,
            AppError.Network.TimeOut() to Res.string.err_ai_offline,
            AppError.Network.RateLimited() to Res.string.err_ai_busy,
            AppError.Network.IntegrityFailed() to Res.string.err_ai_unverified,
            AppError.Network.ServerError(503) to Res.string.err_ai_unavailable,
            AppError.Ai.ApiError(502, "upstream") to Res.string.err_ai_unavailable,
        )
        val viewModel = viewModel()
        viewModel.updatePrompt("anything")

        for ((error, message) in expected) {
            ai.result = Resource.Error(error)
            viewModel.generateMix()
            advanceUntilIdle()

            assertEquals(message, (viewModel.uiState.value.userMessage as UiText.Resource).resId, "for $error")
            viewModel.onMessageShown()
        }
    }

    @Test
    fun aSuggestion_isSentInOneTap() = runMainTest {
        val viewModel = viewModel()

        viewModel.sendSuggestion("Deep sleep")
        advanceUntilIdle()

        assertEquals("Deep sleep", ai.lastPrompt)
        assertEquals("Deep sleep", viewModel.uiState.value.prompt)
        assertEquals("Rainy Night", viewModel.uiState.value.mixName)
    }

    @Test
    fun aSuggestion_stillAsksAboutOpenAiTheFirstTime() = runMainTest {
        preferences.aiDataNoticeAccepted.value = false
        val viewModel = viewModel()

        viewModel.sendSuggestion("Deep sleep")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataNotice)
        assertEquals(null, ai.lastPrompt)
    }

    @Test
    fun theResult_remembersThePromptThatMadeIt() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")
        viewModel.generateMix()
        advanceUntilIdle()

        viewModel.updatePrompt("rain and owls")
        advanceUntilIdle()

        assertEquals("rain", viewModel.uiState.value.lastPrompt)
        assertEquals("rain and owls", viewModel.uiState.value.prompt)
    }

    @Test
    fun anotherVariation_usesThePromptOfTheMixOnScreen() = runMainTest {
        val viewModel = viewModel()
        viewModel.updatePrompt("rain")
        viewModel.generateMix()
        advanceUntilIdle()
        viewModel.updatePrompt("half-typed")

        viewModel.regenerate()
        advanceUntilIdle()

        assertEquals("rain", ai.lastPrompt)
        assertEquals("rain", viewModel.uiState.value.prompt)
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
        assertEquals(mapOf("fire" to EventFrequency.OFTEN), viewModel.uiState.value.sceneCards.frequencies)

        viewModel.changeFrequency("fire", EventFrequency.RARE)
        advanceUntilIdle()

        assertEquals(mapOf("fire" to EventFrequency.RARE), viewModel.uiState.value.sceneCards.frequencies)
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
