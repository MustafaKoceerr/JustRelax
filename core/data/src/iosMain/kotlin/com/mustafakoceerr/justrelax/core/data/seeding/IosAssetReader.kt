package com.mustafakoceerr.justrelax.core.data.seeding

import com.mustafakoceerr.justrelax.core.data.seeding.AssetReader
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.memcpy

class IosAssetReader : AssetReader {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun readAsset(fileName: String): ByteArray {
        // fileName: "config.json" -> name: "config", ext: "json"
        val parts = fileName.split(".")
        val name = parts.dropLast(1).joinToString(".")
        val ext = parts.lastOrNull() ?: ""

        val path = NSBundle.mainBundle.pathForResource(name, ofType = ext)
            ?: throw IllegalArgumentException("Asset not found: $fileName")

        val data = NSData.dataWithContentsOfFile(path)
            ?: throw IllegalArgumentException("Cannot read asset: $fileName")

        // NSData -> ByteArray dönüşümü
        return ByteArray(data.length.toInt()).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    }
}