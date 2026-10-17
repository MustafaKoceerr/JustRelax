package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberAudioPicker(onResult: (uri: String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        onResult(uri?.toString())
    }
    return { launcher.launch(arrayOf("audio/*")) }
}
