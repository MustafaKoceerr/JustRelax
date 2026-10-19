package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Opens the system's document picker for audio files. [onResult] gets the picked document's URI,
 * or null if the user cancelled.
 */
@Composable
fun rememberAudioPicker(onResult: (uri: String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        onResult(uri?.toString())
    }
    return { launcher.launch(arrayOf("audio/*")) }
}
