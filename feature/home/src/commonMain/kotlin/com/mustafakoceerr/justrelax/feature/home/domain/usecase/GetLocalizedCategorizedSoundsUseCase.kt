package com.mustafakoceerr.justrelax.feature.home.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundUi
import com.mustafakoceerr.justrelax.core.model.toSoundUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.collections.mapValues

class GetLocalizedCategorizedSoundsUseCase(
    private val soundRepository: SoundRepository,
    private val languageController: LanguageController
) {
    operator fun invoke(): Flow<Resource<Map<SoundCategory, List<SoundUi>>>> {
        return combine(
            soundRepository.getSounds(),
            languageController.currentLanguage
        ) { sounds, language ->
            sounds.groupBy { SoundCategory.fromId(it.categoryId) }
                .mapValues { (_, categorySounds) ->
                    categorySounds.map { sound ->
                        sound.toSoundUi(language.code)
                    }
                }
        }.asResource()
    }
}