package com.mustafakoceerr.justrelax.feature.mysounds

import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.feature.mysounds.ui.R
import androidx.annotation.StringRes

/** "Loop" for a bed, "One-shot" for an event. */
@get:StringRes
internal val SoundKind.labelRes: Int
    get() = when (this) {
        SoundKind.BED -> R.string.mysounds_kind_bed
        SoundKind.EVENT -> R.string.mysounds_kind_event
    }
