package com.mustafakoceerr.justrelax.core.domain.manager


interface AssetReader{
    /**
     * Asset klasöründeki bir dosyayı ByteArray olarak okur.
     * @param fileName: Dosya adı (örn: "initial_config.json")
     */
    fun readAsset(fileName: String): ByteArray
}