package com.mustafakoceerr.justrelax.core.presentation.util

import com.mustafakoceerr.justrelax.core.model.License

/** License names are the same in every language. */
val License.label: String
    get() = when (this) {
        License.CC0 -> "CC0"
        License.CC_BY_3_0 -> "CC BY 3.0"
        License.CC_BY_4_0 -> "CC BY 4.0"
        License.OWN -> "©"
    }
