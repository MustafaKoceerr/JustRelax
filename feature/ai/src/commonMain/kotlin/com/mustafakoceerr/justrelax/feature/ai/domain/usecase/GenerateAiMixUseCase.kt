package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiGeneratedMix // Import Eklendi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GenerateAiMixUseCase(
    private val aiRepository: AiRepository,
    private val soundRepository: SoundRepository
) {
    // DÜZELTME: Dönüş tipi AiGeneratedMix oldu
    operator fun invoke(prompt: String): Flow<Resource<AiGeneratedMix>> = flow {

        // 1. İndirilmiş sesleri al
        val downloadedSounds = soundRepository.getSounds()
            .first()
            .filter { it.localPath != null }

        if (downloadedSounds.isEmpty()) {
            throw AppError.Ai.NoDownloadedSounds
        }

        val inventoryIds = downloadedSounds.map { it.id }

        // 2. AI İsteği
        val aiResult = aiRepository.generateMix(prompt, inventoryIds)

        // 3. Eşleştirme ve Dönüştürme
        when (aiResult) {
            is Resource.Success -> {
                val response = aiResult.data
                val mixMap = mutableMapOf<Sound, Float>()

                response.sounds.forEach { aiSound ->
                    val match = downloadedSounds.find { it.id == aiSound.id }
                    if (match != null) {
                        val safeVolume = aiSound.volume.coerceIn(0.1f, 1.0f)
                        mixMap[match] = safeVolume
                    }
                }

                if (mixMap.isEmpty()) {
                    throw AppError.Ai.EmptyResponse
                }

                // Ham veriyi zengin modele paketleyip gönderiyoruz
                emit(
                    AiGeneratedMix(
                        name = response.mixName,
                        description = response.description,
                        sounds = mixMap
                    )
                )
            }

            is Resource.Error -> throw aiResult.error
            is Resource.Loading -> { /* Flow.asResource halleder */
            }
        }
    }.asResource()
}