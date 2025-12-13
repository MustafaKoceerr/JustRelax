package com.mustafakoceerr.justrelax.feature.saved.di

import com.mustafakoceerr.justrelax.feature.saved.SavedViewModel
import com.mustafakoceerr.justrelax.feature.saved.usecase.DeleteMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.ObserveSavedMixesUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import org.koin.dsl.module

val savedModule = module {
    factory { DeleteMixUseCase(get()) }
    factory { PlaySavedMixUseCase(get(), get()) }
    factory { ObserveSavedMixesUseCase(get(), get()) }

    factory {
        SavedViewModel(
            savedMixRepository = get(),
            playSavedMixUseCase = get(),
            observeSavedMixesUseCase = get()
        )
    }
}