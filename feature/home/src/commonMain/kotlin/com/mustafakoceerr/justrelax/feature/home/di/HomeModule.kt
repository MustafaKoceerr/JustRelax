package com.mustafakoceerr.justrelax.feature.home.di

import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
    factoryOf(::GetLocalizedCategorizedSoundsUseCase)
}