package com.mustafakoceerr.justrelax.feature.mysounds

import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.feature.mysounds.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mysounds.generated.resources.mysounds_kind_bed
import com.mustafakoceerr.justrelax.feature.mysounds.generated.resources.mysounds_kind_event
import org.jetbrains.compose.resources.StringResource

/** "Loop" for a bed, "One-shot" for an event. */
internal val SoundKind.labelRes: StringResource
    get() = when (this) {
        SoundKind.BED -> Res.string.mysounds_kind_bed
        SoundKind.EVENT -> Res.string.mysounds_kind_event
    }
