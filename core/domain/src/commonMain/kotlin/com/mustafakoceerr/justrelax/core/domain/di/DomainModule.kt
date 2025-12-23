package com.mustafakoceerr.justrelax.core.domain.di

import com.mustafakoceerr.justrelax.core.domain.usecase.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.DownloadSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.SetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.SetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.StopAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.TogglePauseResumeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
/**
 * Bu modül, domain katmanındaki UseCase'leri Koin'e sağlar.
 */
val domainModule = module {
    // --- Data & Sync ---
    // UseCase'ler stateless olduğu için 'factory' (her çağrıda yeni nesne) kullanıyoruz.
    factoryOf(::SyncSoundsIfNecessaryUseCase)
    factoryOf(::DownloadSoundUseCase)

    // --- Player / Audio ---
    // UI katmanı AudioMixer'a sadece bu UseCase'ler üzerinden erişir.
    factoryOf(::PlaySoundUseCase)
    factoryOf(::StopSoundUseCase)
    factoryOf(::StopAllSoundsUseCase)
    factoryOf(::AdjustVolumeUseCase)
    factoryOf(::TogglePauseResumeUseCase)
    factoryOf(::GetPlayingSoundsUseCase)

    // Settings UseCases
    factoryOf(::GetAppThemeUseCase)
    factoryOf(::SetAppThemeUseCase)
    factoryOf(::GetAppLanguageUseCase)
    factoryOf(::SetAppLanguageUseCase)
}