package com.mustafakoceerr.justrelax.feature.saved.di

import com.mustafakoceerr.justrelax.feature.saved.SavedViewModel
import com.mustafakoceerr.justrelax.feature.saved.usecase.PlaySavedMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val savedModule = module {
    factoryOf(::PlaySavedMixUseCase)
    viewModelOf(::SavedViewModel)
}
