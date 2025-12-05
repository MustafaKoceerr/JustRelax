package com.mustafakoceerr.justrelax.core.seeding

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
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