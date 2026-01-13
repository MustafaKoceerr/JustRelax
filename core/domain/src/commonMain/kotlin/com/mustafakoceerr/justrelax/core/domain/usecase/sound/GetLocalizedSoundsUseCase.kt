package com.mustafakoceerr.justrelax.core.domain.usecase.sound

import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.model.SoundUi
import com.mustafakoceerr.justrelax.core.model.toSoundUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetLocalizedSoundsUseCase(
    private val getSoundsUseCase: GetSoundsUseCase,
    private val languageController: LanguageController
) {
    operator fun invoke(): Flow<List<SoundUi>> {
        return combine(
            getSoundsUseCase(),
            languageController.currentLanguage
        ) { sounds, language ->
            sounds.map { sound ->
                sound.toSoundUi(language.code)
            }
        }
    }
}
