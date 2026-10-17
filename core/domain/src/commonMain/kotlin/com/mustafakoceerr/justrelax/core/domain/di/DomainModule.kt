package com.mustafakoceerr.justrelax.core.domain.di

import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetLegalUrlUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadBatchSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val domainModule = module {
    single<Clock> { Clock.System }

    // Data & Sync
    factoryOf(::SyncSoundsIfNecessaryUseCase)
    factoryOf(::SyncSoundsUseCase)
    factoryOf(::DownloadInitialSoundsUseCase)
    factoryOf(::DownloadAllSoundsUseCase)
    factoryOf(::DownloadSingleSoundUseCase)
    factoryOf(::DownloadBatchSoundsUseCase)

    // Player / Audio
    factoryOf(::PlaySoundUseCase)
    factoryOf(::SetMixUseCase)

    // Mix & Legal
    factoryOf(::SaveCurrentMixUseCase)
    factoryOf(::GetLegalUrlUseCase)
}