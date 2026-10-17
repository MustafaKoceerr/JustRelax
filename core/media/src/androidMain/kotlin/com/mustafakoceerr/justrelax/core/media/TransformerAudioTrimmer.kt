package com.mustafakoceerr.justrelax.core.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.GainProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Cuts the range out with Media3 Transformer and re-encodes it as AAC in an MP4 (M4A) file. Both
 * ends get a short fade so a cut through a sound does not click. Transformer runs on the main
 * looper; the encoding itself happens on its own threads.
 */
@UnstableApi
internal class TransformerAudioTrimmer(private val context: Context) : AudioTrimmer {

    override suspend fun trim(sourcePath: String, range: TrimRange, outputPath: String): TrimmedAudio =
        withContext(Dispatchers.Main) {
            val fade = EdgeFade(range.lengthMs, EDGE_FADE_MS)
            val gain = GainProcessor(object : GainProcessor.GainProvider {
                override fun getGainFactorAtSamplePosition(samplePosition: Long, sampleRate: Int) =
                    fade.gainAt(samplePosition, sampleRate)

                override fun isUnityUntil(samplePosition: Long, sampleRate: Int) =
                    fade.unityUntil(samplePosition, sampleRate)
            })
            val item = MediaItem.Builder()
                .setUri(File(sourcePath).toURI().toString())
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(range.startMs)
                        .setEndPositionMs(range.endMs)
                        .build(),
                )
                .build()
            val edited = EditedMediaItem.Builder(item)
                .setRemoveVideo(true)
                .setEffects(Effects(listOf(gain), emptyList()))
                .build()

            suspendCancellableCoroutine { continuation ->
                val transformer = Transformer.Builder(context)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            val size = File(outputPath).length()
                            continuation.resume(TrimmedAudio(durationMs = exportResult.approximateDurationMs, sizeBytes = size))
                        }

                        override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                            File(outputPath).delete()
                            continuation.resumeWithException(MediaException("Trimming failed", exportException))
                        }
                    })
                    .build()
                continuation.invokeOnCancellation {
                    transformer.cancel()
                    File(outputPath).delete()
                }
                transformer.start(edited, outputPath)
            }
        }

    private companion object {
        const val EDGE_FADE_MS = 20L
    }
}
