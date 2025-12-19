package com.mustafakoceerr.justrelax.core.domain.di

import com.mustafakoceerr.justrelax.core.domain.usecase.DownloadSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.SyncSoundsIfNecessaryUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
/**
 * Bu modül, domain katmanındaki UseCase'leri Koin'e sağlar.
 */
val domainModule = module {
    // UseCase'ler genellikle durum (state) tutmazlar, bu yüzden 'factory' olarak
    // tanımlanmaları daha doğrudur. Her istendiğinde yeni bir nesne oluşturulur.
    factoryOf(::SyncSoundsIfNecessaryUseCase)

    // YENİ: DownloadSoundUseCase'i Koin'e tanıt.
    // Koin, constructor'daki 'SoundRepository', 'LocalStorageRepository' ve
    // 'FileDownloadRepository' bağımlılıklarını grafikten otomatik olarak bulup enjekte edecek.
    factoryOf(::DownloadSoundUseCase)
}