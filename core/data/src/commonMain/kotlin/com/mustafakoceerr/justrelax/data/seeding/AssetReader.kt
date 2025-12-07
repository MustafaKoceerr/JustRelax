package com.mustafakoceerr.justrelax.data.seeding

interface AssetReader {
    fun readAsset(fileName: String): ByteArray
}