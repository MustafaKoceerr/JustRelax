package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMix
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMixSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiSound
import com.mustafakoceerr.justrelax.core.network.source.AiNetworkDataSource
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiMixRepositoryImplTest {

    private class FakeAiNetworkDataSource : AiNetworkDataSource {
        var response: Resource<NetworkAiMix> = Resource.Success(NetworkAiMix("Rainy Night", "Soft rain", emptyList()))
        var lastPrompt: String? = null
        var lastInventory: List<NetworkAiSound>? = null

        override suspend fun generateMix(prompt: String, inventory: List<NetworkAiSound>): Resource<NetworkAiMix> {
            lastPrompt = prompt
            lastInventory = inventory
            return response
        }
    }

    private val network = FakeAiNetworkDataSource()
    private val sounds = FakeSoundRepository(
        listOf(
            testSound("rain", localPath = "/sounds/rain.ogg", category = SoundCategory.RAIN)
                .copy(names = mapOf("en" to "Light Rain", "tr" to "Hafif Yağmur"), tags = listOf("rain", "soft")),
            testSound("owl", localPath = "/sounds/owl.ogg", kind = SoundKind.EVENT, defaultFrequency = EventFrequency.RARE),
            testSound("city"),
        )
    )
    private val repository = AiMixRepositoryImpl(network, sounds, FakeLanguageController())

    private fun respondWith(vararg sounds: NetworkAiMixSound) {
        network.response = Resource.Success(NetworkAiMix("Rainy Night", "Soft rain", sounds.toList()))
    }

    private suspend fun generatedMix(): AiGeneratedMix =
        assertIs<Resource.Success<AiGeneratedMix>>(repository.generateMix("x").last()).data

    @Test
    fun theModelSeesTheDownloadedSounds_withNameCategoryKindAndTags() = runTest {
        respondWith(NetworkAiMixSound("rain", 0.5f))

        repository.generateMix("rainy evening").last()

        assertEquals(
            listOf(
                NetworkAiSound("rain", "Light Rain", "RAIN", "BED", listOf("rain", "soft")),
                NetworkAiSound("owl", "owl", "NATURE", "EVENT", emptyList()),
            ),
            network.lastInventory,
        )
        assertTrue(network.lastPrompt!!.startsWith("rainy evening"))
        assertTrue(network.lastPrompt!!.contains("in English language"))
    }

    @Test
    fun theAnswer_becomesAScene_followingEachSoundsKind() = runTest {
        respondWith(NetworkAiMixSound("rain", 0.6f, frequency = "OFTEN"), NetworkAiMixSound("owl", 0.4f, frequency = "OFTEN"))

        val mix = generatedMix()

        assertEquals(
            listOf(Layer.Bed("rain", 0.6f), Layer.Event("owl", 0.4f, EventFrequency.OFTEN)),
            mix.scene.layers,
        )
        assertEquals(listOf("rain", "owl"), mix.sounds.map { it.id })
        assertEquals("Rainy Night", mix.name)
    }

    @Test
    fun anEventWithoutAValidFrequency_usesItsDefault() = runTest {
        respondWith(NetworkAiMixSound("owl", 0.4f, frequency = "SOMETIMES"))

        assertEquals(Layer.Event("owl", 0.4f, EventFrequency.RARE), generatedMix().scene.layers.single())
    }

    @Test
    fun unknownAndRepeatedSoundsAreDropped_andVolumesAreClamped() = runTest {
        respondWith(
            NetworkAiMixSound("rain", 1.5f),
            NetworkAiMixSound("unknown", 0.5f),
            NetworkAiMixSound("rain", 0.2f),
            NetworkAiMixSound("owl", 0.01f),
        )

        val layers = generatedMix().scene.layers

        assertEquals(listOf("rain" to 1.0f, "owl" to 0.1f), layers.map { it.soundId to it.volume })
    }

    @Test
    fun noUsableSounds_isAnEmptyResponse() = runTest {
        respondWith(NetworkAiMixSound("unknown", 0.5f))

        val result = assertIs<Resource.Error>(repository.generateMix("x").last())

        assertIs<AppError.Ai.EmptyResponse>(result.error)
    }

    @Test
    fun nothingDownloaded_failsWithoutAskingTheModel() = runTest {
        sounds.sounds.value = listOf(testSound("city"))

        val result = assertIs<Resource.Error>(repository.generateMix("x").last())

        assertIs<AppError.Ai.NoDownloadedSounds>(result.error)
        assertNull(network.lastPrompt)
    }

    @Test
    fun networkError_isPassedOn() = runTest {
        network.response = Resource.Error(AppError.Ai.ApiError(500, "down"))

        val result = assertIs<Resource.Error>(repository.generateMix("x").last())

        assertIs<AppError.Ai.ApiError>(result.error)
    }
}
