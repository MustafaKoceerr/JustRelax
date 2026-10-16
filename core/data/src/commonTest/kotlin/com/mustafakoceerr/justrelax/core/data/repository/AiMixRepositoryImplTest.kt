package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMix
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMixSound
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
        var lastSoundIds: List<String>? = null

        override suspend fun generateMix(prompt: String, availableSoundIds: List<String>): Resource<NetworkAiMix> {
            lastPrompt = prompt
            lastSoundIds = availableSoundIds
            return response
        }
    }

    private val network = FakeAiNetworkDataSource()
    private val sounds = FakeSoundRepository(
        listOf(
            testSound("rain", localPath = "/sounds/rain.mp3"),
            testSound("fire", localPath = "/sounds/fire.mp3"),
            testSound("city"),
        )
    )
    private val repository = AiMixRepositoryImpl(network, sounds, FakeLanguageController())

    private fun respondWith(vararg sounds: Pair<String, Float>) {
        network.response = Resource.Success(
            NetworkAiMix("Rainy Night", "Soft rain", sounds.map { (id, volume) -> NetworkAiMixSound(id, volume) })
        )
    }

    @Test
    fun onlyDownloadedSoundsAreOffered_andTheAnswerLanguageIsRequested() = runTest {
        respondWith("rain" to 0.5f)

        repository.generateMix("rainy evening").last()

        assertEquals(listOf("rain", "fire"), network.lastSoundIds)
        assertTrue(network.lastPrompt!!.startsWith("rainy evening"))
        assertTrue(network.lastPrompt!!.contains("in English language"))
    }

    @Test
    fun unknownSoundsAreDropped_andVolumesAreClamped() = runTest {
        respondWith("rain" to 1.5f, "unknown" to 0.5f, "fire" to 0.01f)

        val mix = assertIs<Resource.Success<AiGeneratedMix>>(repository.generateMix("x").last()).data

        assertEquals("Rainy Night", mix.name)
        assertEquals(mapOf("rain" to 1.0f, "fire" to 0.1f), mix.sounds.mapKeys { it.key.id })
    }

    @Test
    fun noUsableSounds_isAnEmptyResponse() = runTest {
        respondWith("unknown" to 0.5f)

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
