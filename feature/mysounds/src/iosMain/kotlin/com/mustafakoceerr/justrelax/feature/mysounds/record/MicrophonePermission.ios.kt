package com.mustafakoceerr.justrelax.feature.mysounds.record

import androidx.compose.runtime.Composable

// iOS has no recorder yet; the recorder reports the failure.
@Composable
actual fun rememberMicrophonePermission(onResult: (granted: Boolean) -> Unit): () -> Unit = { onResult(true) }
