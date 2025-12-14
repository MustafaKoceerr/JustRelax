package com.mustafakoceerr.justrelax.feature.ai.di

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.GetActiveSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.feature.ai.AiViewModel
import com.mustafakoceerr.justrelax.feature.ai.data.repository.AiServiceImpl
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiService
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveActiveContextUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveDownloadedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.PlayAiMixUseCase
import org.koin.dsl.module

val aiModule = module {
    // Service Implementation
    single<AiService> { AiServiceImpl(get(), get()) }

    // UseCases
    factory { PlayAiMixUseCase(get(), get()) }
    factory { ObserveDownloadedSoundsUseCase(get()) }
    factory { ObserveActiveContextUseCase(get()) }

    // ViewModel
    factory {
        AiViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}
