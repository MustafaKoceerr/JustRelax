package com.mustafakoceerr.justrelax.data.seeding

import android.content.Context

class AndroidAssetReader(private val context: Context) : AssetReader {
    override fun readAsset(fileName: String): ByteArray {
        return context.assets.open(fileName).use { it.readBytes() }
    }
}