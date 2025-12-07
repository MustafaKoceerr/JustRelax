package com.mustafakoceerr.justrelax.data.seeding

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.memcpy

class IosAssetReader : AssetReader {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun readAsset(fileName: String): ByteArray {
        val parts = fileName.split(".")
        val name = parts.dropLast(1).joinToString(".")
        val ext = parts.lastOrNull() ?: ""

        val path = NSBundle.mainBundle.pathForResource(name, ofType = ext)
            ?: throw IllegalArgumentException("Asset not found: $fileName")

        val data = NSData.dataWithContentsOfFile(path)
            ?: throw IllegalArgumentException("Cannot read asset: $fileName")

        return ByteArray(data.length.toInt()).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    }
}