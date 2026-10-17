package com.mustafakoceerr.justrelax.core.media.di

import com.mustafakoceerr.justrelax.core.media.AudioImporter
import com.mustafakoceerr.justrelax.core.media.AudioRecorder
import com.mustafakoceerr.justrelax.core.media.AudioTrimmer
import com.mustafakoceerr.justrelax.core.media.ClipPreviewer
import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.media.TrimmedAudio
import com.mustafakoceerr.justrelax.core.media.Waveform
import com.mustafakoceerr.justrelax.core.media.WaveformReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module

// iOS has no media implementation yet: importing fails with a message instead of crashing.
internal actual val platformMediaModule = module {
    single<WaveformReader> { UnsupportedMedia }
    single<AudioTrimmer> { UnsupportedMedia }
    single<AudioImporter> { UnsupportedMedia }
    factory<ClipPreviewer> { SilentPreviewer() }
    factory<AudioRecorder> { UnsupportedRecorder }
}

private object UnsupportedMedia : WaveformReader, AudioTrimmer, AudioImporter {
    override suspend fun read(path: String, bucketCount: Int): Waveform = unsupported()
    override suspend fun trim(sourcePath: String, range: TrimRange, outputPath: String): TrimmedAudio = unsupported()
    override suspend fun import(uri: String): ImportedAudio = unsupported()
    private fun unsupported(): Nothing = throw MediaException("Not supported on iOS yet")
}

private object UnsupportedRecorder : AudioRecorder {
    override fun start(): String = throw MediaException("Not supported on iOS yet")
    override fun level() = 0f
    override fun stop() = Unit
    override fun cancel() = Unit
}

private class SilentPreviewer : ClipPreviewer {
    override val isPlaying: StateFlow<Boolean> = MutableStateFlow(false)
    override fun play(path: String, range: TrimRange, loop: Boolean) = Unit
    override fun stop() = Unit
    override fun release() = Unit
}
