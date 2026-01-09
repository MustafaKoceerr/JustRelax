package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GenerateAiMixUseCase(
    private val aiRepository: AiRepository,
    private val soundRepository: SoundRepository
) {
    operator fun invoke(prompt: String): Flow<Resource<AiGeneratedMix>> = flow {
        val allSounds = soundRepository.getSounds().first()
        val downloadedSounds = allSounds.filter { it.localPath != null }

        if (downloadedSounds.isEmpty()) {
            throw AppError.Ai.NoDownloadedSounds()
        }

        val inventoryIds = downloadedSounds.map { it.id }

        val response = when (val aiResult = aiRepository.generateMix(prompt, inventoryIds)) {
            is Resource.Success -> aiResult.data
            is Resource.Error -> throw aiResult.error
            is Resource.Loading -> return@flow
        }

        val mixMap = mutableMapOf<Sound, Float>()

        response.sounds.forEach { aiSound ->
            val match = downloadedSounds.find { it.id == aiSound.id }
            if (match != null) {
                val safeVolume = aiSound.volume.coerceIn(0.1f, 1.0f)
                mixMap[match] = safeVolume
            }
        }

        if (mixMap.isEmpty()) {
            throw AppError.Ai.EmptyResponse()
        }

        emit(
            AiGeneratedMix(
                name = response.mixName,
                description = response.description,
                sounds = mixMap
            )
        )
    }.asResource()
}