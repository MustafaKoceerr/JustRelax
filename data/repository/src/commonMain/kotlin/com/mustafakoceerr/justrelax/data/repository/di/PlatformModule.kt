package com.mustafakoceerr.justrelax.data.repository.di

import org.koin.core.module.Module

/*
Bu dosya, :data:repository modülünün commonMain'inde yer alacak ve platformlara "Bana ObservableSettings gibi platforma özel şeyleri sağlayan bir Koin modülü verin" diyecek.
 */

/**
 * Platforma özel bağımlılıkları (DataStore) sağlayacak olan
 * Koin modülünü 'bekler'.
 */
internal expect val platformRepositoryModule: Module