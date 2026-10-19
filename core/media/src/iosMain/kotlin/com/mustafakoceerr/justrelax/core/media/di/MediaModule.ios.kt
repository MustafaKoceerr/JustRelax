package com.mustafakoceerr.justrelax.core.media.di

import com.mustafakoceerr.justrelax.core.media.AudioImporter
import com.mustafakoceerr.justrelax.core.media.AudioTrimmer
import com.mustafakoceerr.justrelax.core.media.AvAudioTrimmer
import com.mustafakoceerr.justrelax.core.media.AvClipPreviewer
import com.mustafakoceerr.justrelax.core.media.AvWaveformReader
import com.mustafakoceerr.justrelax.core.media.ClipPreviewer
import com.mustafakoceerr.justrelax.core.media.IosAudioImporter
import com.mustafakoceerr.justrelax.core.media.ScenePauser
import com.mustafakoceerr.justrelax.core.media.WaveformReader
import org.koin.dsl.module

internal actual val platformMediaModule = module {
    single<WaveformReader> { AvWaveformReader(get()) }
    single<AudioTrimmer> { AvAudioTrimmer(get()) }
    single<AudioImporter> { IosAudioImporter(get()) }
    factory<ClipPreviewer> { AvClipPreviewer(getOrNull<ScenePauser>() ?: ScenePauser {}) }
}
