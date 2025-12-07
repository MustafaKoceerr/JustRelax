package com.mustafakoceerr.justrelax.core.data.seeding

import android.content.Context
import com.mustafakoceerr.justrelax.core.data.seeding.AssetReader

class AndroidAssetReader(private val context: Context) : AssetReader {
    override fun readAsset(fileName: String): ByteArray {
        return context.assets.open(fileName).use { it.readBytes() }
    }
}