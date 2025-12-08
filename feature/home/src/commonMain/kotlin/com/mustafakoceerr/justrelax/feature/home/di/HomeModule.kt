package com.mustafakoceerr.justrelax.feature.home.di

import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import com.mustafakoceerr.justrelax.feature.home.usecase.DismissBannerUseCase
import com.mustafakoceerr.justrelax.feature.home.usecase.DownloadAllMissingSoundsUseCase
import com.mustafakoceerr.justrelax.feature.home.usecase.ShouldShowBannerUseCase
import org.koin.dsl.module

val homeModule = module {
    factory { DownloadAllMissingSoundsUseCase(get(), get()) }
    factory { ShouldShowBannerUseCase(get()) }
    factory { DismissBannerUseCase(get()) }

    // ViewModel artık 6 parametre alıyor
    factory {
        HomeViewModel(
            soundRepository = get(),
            shouldShowBannerUseCase = get(),
            dismissBannerUseCase = get(),
            downloadAllMissingSoundsUseCase = get(),
            soundManager = get(),
            toggleSoundUseCase = get()
        )
    }
}