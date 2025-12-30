package com.mustafakoceerr.justrelax.feature.home.di

import com.mustafakoceerr.justrelax.feature.home.HomeScreenModel
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.GetCategorizedSoundsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homeModule = module {
    // Voyager ScreenModel'leri de genellikle 'factory' ile oluşturulur.
    // Çünkü ekran her yaratıldığında (Lifecycle) yeni bir tane istenir.
    factoryOf(::HomeScreenModel)
    factoryOf(::GetCategorizedSoundsUseCase)

}