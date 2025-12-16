package com.mustafakoceerr.justrelax.core.data.seeding

import android.content.Context
import com.mustafakoceerr.justrelax.core.domain.manager.AssetReader

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