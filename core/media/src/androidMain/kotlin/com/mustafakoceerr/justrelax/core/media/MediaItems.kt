package com.mustafakoceerr.justrelax.core.media

import androidx.media3.common.MediaItem

/** A media item that plays or exports only [range] of the file or URL at [uri]. */
internal fun clippedMediaItem(uri: String, range: TrimRange): MediaItem =
    MediaItem.Builder()
        .setUri(uri)
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(range.startMs)
                .setEndPositionMs(range.endMs)
                .build(),
        )
        .build()
