package com.mustafakoceerr.justrelax.core.data.download

/**
 * Counts every download as successful and adds the destination file to [storage].
 * Tests change the behaviour (delay, failure, exception) through [onDownload].
 */
internal class FakeFileDownloader(
    private val storage: FakeSoundFileStorage? = null,
    var onDownload: suspend (url: String) -> Boolean = { true }
) : FileDownloader {

    val requestedUrls = mutableListOf<String>()

    override suspend fun downloadFile(url: String, destinationPath: String): Boolean {
        requestedUrls += url
        val success = onDownload(url)
        if (success) storage?.files?.add(destinationPath)
        return success
    }
}
