package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.system.FileDownloadRepository

/**
 * Varsayılan olarak her indirmeyi başarılı sayar ve hedef dosyayı [storage]'a ekler.
 * [onDownload] ile davranış (gecikme, hata, exception) testten özelleştirilebilir.
 */
class FakeFileDownloadRepository(
    private val storage: FakeLocalStorageRepository? = null,
    var onDownload: suspend (url: String) -> Boolean = { true }
) : FileDownloadRepository {

    val requestedUrls = mutableListOf<String>()

    override suspend fun downloadFile(url: String, destinationPath: String): Boolean {
        requestedUrls += url
        val success = onDownload(url)
        if (success) storage?.files?.add(destinationPath)
        return success
    }
}
