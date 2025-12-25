package com.mustafakoceerr.justrelax.feature.ai.di

import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.feature.ai.AiScreenModel
import com.mustafakoceerr.justrelax.feature.ai.data.repository.OpenAiRepositoryImpl
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.GenerateAiMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aiModule = module {
    // --- Data Katmanı ---
    // Single: AiRepository'nin durumu (state) yoktur, tek bir instance yeterlidir.
    single<AiRepository> {
        OpenAiRepositoryImpl(client = get())
    }

    factoryOf(::GenerateAiMixUseCase)
    factoryOf(::AiScreenModel)
}
