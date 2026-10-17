package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.compose.runtime.Composable

// iOS has no import yet; the button does nothing.
@Composable
actual fun rememberAudioPicker(onResult: (uri: String?) -> Unit): () -> Unit = { onResult(null) }
