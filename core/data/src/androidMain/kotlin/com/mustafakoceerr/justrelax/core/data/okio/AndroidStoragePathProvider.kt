package com.mustafakoceerr.justrelax.core.data.okio

import android.content.Context
import com.mustafakoceerr.justrelax.core.domain.manager.StoragePathProvider
import okio.Path
import okio.Path.Companion.toPath

class AndroidStoragePathProvider (
    private val context: Context
): StoragePathProvider {
    override fun getAppDataDir(): Path {
        // Context güvenli bir şekilde inject edildi
        return context.filesDir.absolutePath.toPath()
    }

    override fun getCacheDir(): Path {
        return context.cacheDir.absolutePath.toPath()
    }

}