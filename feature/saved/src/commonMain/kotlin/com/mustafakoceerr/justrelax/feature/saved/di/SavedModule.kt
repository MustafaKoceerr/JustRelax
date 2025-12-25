package com.mustafakoceerr.justrelax.feature.saved.di

import com.mustafakoceerr.justrelax.feature.saved.SavedScreenModel
import com.mustafakoceerr.justrelax.feature.saved.usecase.DeleteSavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.ObserveSavedMixesUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.usecase.RestoreSavedMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val savedModule = module {
    // UseCases
    factoryOf(::ObserveSavedMixesUseCase)
    factoryOf(::PlaySavedMixUseCase)
    factoryOf(::DeleteSavedMixUseCase)  // YENİ
    factoryOf(::RestoreSavedMixUseCase) // YENİ

    // ScreenModel
    factoryOf(::SavedScreenModel)
}