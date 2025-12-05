package com.mustafakoceerr.justrelax.core.okio

import okio.Path

interface StoragePathProvider {
    fun getAppDataDir(): Path
}