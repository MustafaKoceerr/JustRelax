package com.mustafakoceerr.justrelax.feature.mysounds.record

import androidx.compose.runtime.Composable

/** Asks for the microphone if needed; [onResult] gets whether recording is allowed. */
@Composable
expect fun rememberMicrophonePermission(onResult: (granted: Boolean) -> Unit): () -> Unit
