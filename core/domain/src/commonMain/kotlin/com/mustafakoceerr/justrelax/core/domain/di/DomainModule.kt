package com.mustafakoceerr.justrelax.core.domain.di

import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.GetAppSetupStatusUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.SetAppSetupFinishedUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PauseAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.ResumeAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.TogglePauseResumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetLegalUrlUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.GetSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadBatchSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    // Data & Sync
    factoryOf(::SyncSoundsIfNecessaryUseCase)
    factoryOf(::SyncSoundsUseCase)
    factoryOf(::DownloadInitialSoundsUseCase)
    factoryOf(::DownloadAllSoundsUseCase)
    factoryOf(::DownloadSingleSoundUseCase)
    factoryOf(::DownloadBatchSoundsUseCase)
    factoryOf(::GetSoundsUseCase)

    // Player / Audio
    factoryOf(::PlaySoundUseCase)
    factoryOf(::StopSoundUseCase)
    factoryOf(::StopAllSoundsUseCase)
    factoryOf(::AdjustVolumeUseCase)
    factoryOf(::TogglePauseResumeUseCase)
    factoryOf(::GetGlobalMixerStateUseCase)
    factoryOf(::PauseAllSoundsUseCase)
    factoryOf(::ResumeAllSoundsUseCase)
    factoryOf(::SetMixUseCase)

    // Settings
    factoryOf(::GetAppThemeUseCase)
    factoryOf(::SetAppThemeUseCase)
    factoryOf(::GetAppLanguageUseCase)
    factoryOf(::SetAppLanguageUseCase)

    // App Setup
    factoryOf(::GetAppSetupStatusUseCase)
    factoryOf(::SetAppSetupFinishedUseCase)

    // Mix & Legal
    factoryOf(::SaveCurrentMixUseCase)
    factoryOf(::GetLegalUrlUseCase)
}