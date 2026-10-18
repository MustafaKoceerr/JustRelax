package com.mustafakoceerr.justrelax.core.media.di

import com.mustafakoceerr.justrelax.core.media.AndroidAudioImporter
import com.mustafakoceerr.justrelax.core.media.AndroidWaveformReader
import com.mustafakoceerr.justrelax.core.media.AudioImporter
import com.mustafakoceerr.justrelax.core.media.AudioTrimmer
import com.mustafakoceerr.justrelax.core.media.ClipPreviewer
import com.mustafakoceerr.justrelax.core.media.ExoClipPreviewer
import com.mustafakoceerr.justrelax.core.media.TransformerAudioTrimmer
import com.mustafakoceerr.justrelax.core.media.WaveformReader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformMediaModule = module {
    single<WaveformReader> { AndroidWaveformReader(get()) }
    single<AudioTrimmer> { TransformerAudioTrimmer(androidContext()) }
    single<AudioImporter> { AndroidAudioImporter(androidContext(), get()) }
    factory<ClipPreviewer> { ExoClipPreviewer(androidContext()) }
}
