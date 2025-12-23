package com.mustafakoceerr.justrelax.feature.home.di

import com.mustafakoceerr.justrelax.feature.home.HomeScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homeModule = module {
    // Voyager ScreenModel'leri de genellikle 'factory' ile oluşturulur.
    // Çünkü ekran her yaratıldığında (Lifecycle) yeni bir tane istenir.
    factoryOf(::HomeScreenModel)

    // Eğer HomeBannerUseCases henüz bir modüle eklenmediyse:
    // (Legacy koddan geldiği için buraya veya domain modülüne ekleyebilirsin)
//    factoryOf(::HomeBannerUseCases)
}