package com.mustafakoceerr.justrelax.core.seeding

import android.content.Context

class AndroidAssetReader(
    private val context: Context
) : AssetReader {
    override fun readAsset(fileName: String): ByteArray {
        // use bloğu stream'i otomatik kapatır (Memory Leak önler)
        return context.assets.open(fileName).use { inputStream ->
            inputStream.readBytes()
        }
    }


}