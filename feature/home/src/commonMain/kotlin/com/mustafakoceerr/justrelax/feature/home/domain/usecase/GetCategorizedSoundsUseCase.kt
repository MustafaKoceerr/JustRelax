package com.mustafakoceerr.justrelax.feature.home.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetCategorizedSoundsUseCase(
    private val soundRepository: SoundRepository
) {
    operator fun invoke(): Flow<Resource<Map<SoundCategory, List<Sound>>>> {
        return soundRepository.getSounds()
            .map { sounds ->
                sounds.groupBy { SoundCategory.fromId(it.categoryId) }
            }
            .asResource()
    }
}