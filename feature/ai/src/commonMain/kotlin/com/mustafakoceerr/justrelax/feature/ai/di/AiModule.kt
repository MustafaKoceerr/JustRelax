package com.mustafakoceerr.justrelax.feature.ai.di

import com.mustafakoceerr.justrelax.feature.ai.AiViewModel
import com.mustafakoceerr.justrelax.feature.ai.data.repository.AiServiceImpl
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiService
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveDownloadedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.PlayAiMixUseCase
import org.koin.dsl.module

val aiModule = module {
    // Service Implementation
    single<AiService> { AiServiceImpl(get(), get()) }

    // UseCases
    factory { PlayAiMixUseCase(get(), get()) }
    factory { ObserveDownloadedSoundsUseCase(get()) }

    // ViewModel
    factory {
        AiViewModel(
            aiService = get(),
            playAiMixUseCase = get(),
            observeDownloadedSoundsUseCase = get()
        )
    }
}