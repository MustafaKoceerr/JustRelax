package com.mustafakoceerr.justrelax.core.okio

import android.content.Context
import okio.Path
import okio.Path.Companion.toPath

class AndroidStoragePathProvider (
    private val context: Context
): StoragePathProvider{
    override fun getAppDataDir(): Path {
        // Context güvenli bir şekilde inject edildi
        return context.filesDir.absolutePath.toPath()
    }

}