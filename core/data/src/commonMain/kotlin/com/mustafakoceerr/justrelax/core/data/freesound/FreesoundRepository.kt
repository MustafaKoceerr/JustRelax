package com.mustafakoceerr.justrelax.core.data.freesound

import com.mustafakoceerr.justrelax.core.data.download.FileDownloader
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.network.dto.NetworkFreesoundSound
import com.mustafakoceerr.justrelax.core.network.source.FreesoundDataSource
import kotlin.math.roundToLong

data class FreesoundResults(val sounds: List<FreesoundSound>, val hasMore: Boolean)

/** Searches Freesound and fetches previews; adding a result to the library goes through the editor. */
interface FreesoundRepository {
    /** @throws com.mustafakoceerr.justrelax.core.common.AppError.Network */
    suspend fun search(query: String, page: Int): FreesoundResults

    /** Downloads the sound's preview for the editor; null if it could not be downloaded. */
    suspend fun downloadPreview(sound: FreesoundSound): String?
}

/** The preview files Freesound serves for each sound. */
internal enum class FreesoundPreview(val key: String) {
    OGG("preview-hq-ogg"),
    MP3("preview-hq-mp3"),
}

/**
 * Which preview to use, best first, on this platform. Android prefers Ogg: it loops without a gap
 * and SoundPool plays it. iOS cannot open Ogg Vorbis (AVFoundation reads only Ogg Opus), so it
 * takes the MP3.
 */
internal expect val preferredFreesoundPreviews: List<FreesoundPreview>

internal class NetworkFreesoundRepository(
    private val dataSource: FreesoundDataSource,
    private val downloader: FileDownloader,
    private val storage: SoundFileStorage,
    private val previews: List<FreesoundPreview> = listOf(FreesoundPreview.OGG, FreesoundPreview.MP3),
) : FreesoundRepository {

    override suspend fun search(query: String, page: Int): FreesoundResults {
        val result = dataSource.search(query, page)
        return FreesoundResults(result.sounds.mapNotNull { it.asFreesoundSoundOrNull(previews) }, result.hasMore)
    }

    override suspend fun downloadPreview(sound: FreesoundSound): String? {
        val extension = sound.previewUrl.substringAfterLast('/').substringAfterLast('.', missingDelimiterValue = "audio")
        val path = "${storage.getIncomingDirectoryPath()}/freesound-${sound.id}.$extension"
        return path.takeIf { downloader.downloadFile(sound.previewUrl, path) }
    }
}

/** Null for licenses the app cannot use (non-commercial, sampling+) and for sounds without a preview. */
internal fun NetworkFreesoundSound.asFreesoundSoundOrNull(order: List<FreesoundPreview>): FreesoundSound? {
    val license = creativeCommonsLicense(license) ?: return null
    val preview = order.firstNotNullOfOrNull { previews[it.key] } ?: return null
    return FreesoundSound(
        id = id,
        name = name,
        author = username,
        license = license,
        pageUrl = url,
        tags = tags,
        durationMs = (duration * 1_000).roundToLong(),
        previewUrl = preview,
    )
}

private fun creativeCommonsLicense(url: String): License? {
    val path = url.substringAfter("creativecommons.org/", missingDelimiterValue = "").trimEnd('/')
    return when (path) {
        "publicdomain/zero/1.0" -> License.CC0
        "licenses/by/3.0" -> License.CC_BY_3_0
        "licenses/by/4.0" -> License.CC_BY_4_0
        else -> null
    }
}
