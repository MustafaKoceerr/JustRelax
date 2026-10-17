package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.localized
import com.mustafakoceerr.justrelax.core.network.source.AiNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class AiMixRepositoryImpl(
    private val aiDataSource: AiNetworkDataSource,
    private val soundRepository: SoundRepository,
    private val languageController: LanguageController
) : AiMixRepository {

    override fun generateMix(prompt: String): Flow<Resource<AiGeneratedMix>> = flow {
        val downloadedSounds = soundRepository.getSounds().first().filter { it.localPath != null }

        if (downloadedSounds.isEmpty()) {
            throw AppError.Ai.NoDownloadedSounds()
        }

        val currentLanguage = languageController.getCurrentLanguage()
        val modifiedPrompt = """
            $prompt

            IMPORTANT: Provide the 'mix_name' and 'description' in ${currentLanguage.aiPromptName} language.
        """.trimIndent()

        val response = when (val aiResult = aiDataSource.generateMix(modifiedPrompt, downloadedSounds.map { it.id })) {
            is Resource.Success -> aiResult.data
            is Resource.Error -> throw aiResult.error
            is Resource.Loading -> return@flow
        }

        val mixMap = mutableMapOf<LocalizedSound, Float>()
        response.sounds.forEach { aiSound ->
            val match = downloadedSounds.find { it.id == aiSound.id }
            if (match != null) {
                mixMap[match.localized(currentLanguage.code)] = aiSound.volume.coerceIn(0.1f, 1.0f)
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
