package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.localized
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiMixSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAiSound
import com.mustafakoceerr.justrelax.core.network.source.AiNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Describes the downloaded sounds to the model (name, category, kind, tags) and turns its answer
 * into a scene. Whether a sound is a bed or an event comes from the library, not from the model.
 */
internal class AiMixRepositoryImpl(
    private val aiDataSource: AiNetworkDataSource,
    private val soundRepository: SoundRepository,
    private val languageController: LanguageController
) : AiMixRepository {

    override fun generateMix(prompt: String): Flow<Resource<AiGeneratedMix>> = flow {
        val downloadedSounds = soundRepository.getSounds().first().filter { it.isDownloaded }

        if (downloadedSounds.isEmpty()) {
            throw AppError.Ai.NoDownloadedSounds()
        }

        val currentLanguage = languageController.getCurrentLanguage()
        val inventory = downloadedSounds.map { it.asInventoryItem() }

        val response = when (val aiResult = aiDataSource.generateMix(prompt, inventory, currentLanguage.aiPromptName)) {
            is Resource.Success -> aiResult.data
            is Resource.Error -> throw aiResult.error
            is Resource.Loading -> return@flow
        }

        val soundsById = downloadedSounds.associateBy { it.id }
        val chosen = response.sounds
            .distinctBy { it.id }
            .mapNotNull { answer -> soundsById[answer.id]?.let { it to answer } }
            .take(Scene.MAX_LAYERS)

        if (chosen.isEmpty()) {
            throw AppError.Ai.EmptyResponse()
        }

        emit(
            AiGeneratedMix(
                name = response.mixName,
                description = response.description,
                scene = Scene(chosen.map { (sound, answer) -> layerFor(sound, answer) }),
                sounds = chosen.map { (sound, _) -> sound.localized(currentLanguage.code) },
            )
        )
    }.asResource()

    private fun Sound.asInventoryItem() = NetworkAiSound(
        id = id,
        name = getDisplayName("en"),
        category = category.id,
        kind = kind.name,
        tags = tags,
    )

    private fun layerFor(sound: Sound, answer: NetworkAiMixSound): Layer {
        val volume = answer.volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
        return when (sound.kind) {
            SoundKind.BED -> Layer.Bed(sound.id, volume)
            SoundKind.EVENT -> Layer.Event(
                soundId = sound.id,
                volume = volume,
                frequency = EventFrequency.entries.find { it.name == answer.frequency } ?: sound.defaultFrequency,
            )
        }
    }

    private companion object {
        const val MIN_VOLUME = 0.1f
        const val MAX_VOLUME = 1.0f
    }
}
