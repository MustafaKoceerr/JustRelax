package com.mustafakoceerr.justrelax.feature.library.di

import com.mustafakoceerr.justrelax.feature.library.LibraryViewModel
import com.mustafakoceerr.justrelax.feature.library.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryModule = module {
    viewModelOf(::LibraryViewModel)
    factoryOf(::GetLocalizedCategorizedSoundsUseCase)
}