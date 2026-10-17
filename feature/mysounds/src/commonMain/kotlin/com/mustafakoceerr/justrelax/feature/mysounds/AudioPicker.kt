package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.compose.runtime.Composable

/**
 * Opens the system's document picker for audio files. [onResult] gets the picked document's URI,
 * or null if the user cancelled.
 */
@Composable
expect fun rememberAudioPicker(onResult: (uri: String?) -> Unit): () -> Unit
