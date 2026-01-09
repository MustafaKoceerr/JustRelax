package com.mustafakoceerr.justrelax.feature.ai.di

import com.mustafakoceerr.justrelax.feature.ai.AiViewModel
import com.mustafakoceerr.justrelax.feature.ai.data.repository.OpenAiRepositoryImpl
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.GenerateAiMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aiModule = module {
    single<AiRepository> {
        OpenAiRepositoryImpl()
    }

    factoryOf(::GenerateAiMixUseCase)
    factoryOf(::AiViewModel)
}