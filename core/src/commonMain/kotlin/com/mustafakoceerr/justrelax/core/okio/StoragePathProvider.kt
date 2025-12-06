package com.mustafakoceerr.justrelax.core.okio

import okio.Path

interface StoragePathProvider {
    fun getAppDataDir(): Path // Mevcut (Files/Documents)
    fun getCacheDir(): Path // YENİ (Geçici/Önbellek dosyaları için)
}