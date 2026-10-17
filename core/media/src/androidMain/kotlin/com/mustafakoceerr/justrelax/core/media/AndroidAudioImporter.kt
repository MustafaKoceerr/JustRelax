package com.mustafakoceerr.justrelax.core.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Copies a document from the system picker into `cache/import`. The copy outlives the picker's
 * temporary read permission, so the editor still works after the app is restored.
 */
internal class AndroidAudioImporter(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) : AudioImporter {

    override suspend fun import(uri: String): ImportedAudio = withContext(dispatchers.io) {
        val document = Uri.parse(uri)
        val directory = File(context.cacheDir, IMPORT_DIRECTORY).apply { mkdirs() }
        val partial = File(directory, "$IMPORT_FILE.part")
        val copy = File(directory, IMPORT_FILE)
        try {
            val input = context.contentResolver.openInputStream(document) ?: throw MediaException("Cannot open $uri")
            input.use { source ->
                partial.outputStream().use { target ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_BYTES) throw MediaException("Larger than ${MAX_BYTES / 1_048_576} MB")
                        target.write(buffer, 0, read)
                    }
                }
            }
            if (!partial.renameTo(copy)) throw MediaException("Cannot store the copy")
            ImportedAudio(copy.path, displayName(document))
        } catch (e: IOException) {
            throw MediaException("Cannot read $uri", e)
        } catch (e: SecurityException) {
            throw MediaException("No access to $uri", e)
        } finally {
            partial.delete()
        }
    }

    private fun displayName(document: Uri): String? = runCatching {
        context.contentResolver.query(document, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }.getOrNull()

    private companion object {
        const val IMPORT_DIRECTORY = "import"
        const val IMPORT_FILE = "audio"
        const val MAX_BYTES = 200L * 1_048_576
    }
}
