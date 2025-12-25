package com.mustafakoceerr.justrelax.core.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Tıklama olaylarını belirli bir süre (debounceMs) filtreleyen yardımcı fonksiyon.
 * Monotonic zaman kaynağı kullanarak sistem saati değişimlerinden etkilenmez.
 */
@Composable
fun rememberDebouncedOnClick(
    debounceMs: Long = 300L,
    onClick: () -> Unit
): () -> Unit {
    val currentOnClick by rememberUpdatedState(onClick)
    val debounceDuration = debounceMs.milliseconds

    return remember(debounceMs) {
        // Başlangıç değerini geçmişte ayarlıyoruz ki ilk tıklama hemen çalışsın.
        var lastClickMark = TimeSource.Monotonic.markNow() - debounceDuration

        {
            if (lastClickMark.elapsedNow() >= debounceDuration) {
                lastClickMark = TimeSource.Monotonic.markNow()
                currentOnClick()
            }
        }
    }
}