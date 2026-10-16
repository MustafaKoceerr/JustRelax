package com.mustafakoceerr.justrelax.core.data.download

internal fun interface FileDownloader {
    /** Returns true only if the whole file was written to [destinationPath]. */
    suspend fun downloadFile(url: String, destinationPath: String): Boolean
}
