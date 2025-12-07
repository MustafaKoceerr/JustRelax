package com.mustafakoceerr.justrelax.core.data.seeding

interface AssetReader {
    fun readAsset(fileName: String): ByteArray
}