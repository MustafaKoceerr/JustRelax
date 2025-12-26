package com.mustafakoceerr.justrelax.core.domain.di

import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.GetSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.TogglePauseResumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.GetAppSetupStatusUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.SetAppSetupFinishedUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.CheckMaxActiveSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.ObservePlaybackStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PauseAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.ResumeAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetLegalUrlUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SyncLanguageWithSystemUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadBatchSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Bu modül, domain katmanındaki UseCase'leri Koin'e sağlar.
 */
val domainModule = module {
    // --- Data & Sync ---
    // UseCase'ler stateless olduğu için 'factory' (her çağrıda yeni nesne) kullanıyoruz.
    factoryOf(::SyncSoundsIfNecessaryUseCase)

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

    factoryOf(::SyncSoundsUseCase)
    factoryOf(::DownloadInitialSoundsUseCase)
    factoryOf(::DownloadAllSoundsUseCase)

    // --- YENİ EKLENENLER ---
    factoryOf(::GetSoundsUseCase)
    factoryOf(::GetAppSetupStatusUseCase)
    factoryOf(::SetAppSetupFinishedUseCase)
    factoryOf(::CheckMaxActiveSoundsUseCase)
    factoryOf(::SyncLanguageWithSystemUseCase)

    factoryOf(::PauseAllSoundsUseCase)
    factoryOf(::ResumeAllSoundsUseCase)
    factoryOf(::ObservePlaybackStateUseCase)
    factoryOf(::SetMixUseCase)
    factoryOf(::SaveCurrentMixUseCase)

    // 2. Beyin (Batch Engine)
    factoryOf(::DownloadSingleSoundUseCase)
    factoryOf(::DownloadBatchSoundsUseCase)

    factoryOf(::SaveCurrentMixUseCase)
    factoryOf(::SaveCurrentMixUseCase)
    factoryOf(::GetLegalUrlUseCase)

}