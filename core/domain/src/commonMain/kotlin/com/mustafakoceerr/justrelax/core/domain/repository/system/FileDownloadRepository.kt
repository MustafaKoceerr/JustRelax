package com.mustafakoceerr.justrelax.core.domain.repository.system

interface FileDownloadRepository {
    suspend fun downloadFile(url: String, destinationPath: String): Boolean
}