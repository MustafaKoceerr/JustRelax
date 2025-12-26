package com.mustafakoceerr.justrelax.core.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Tıklama olaylarını belirli bir süre (debounceMs) filtreleyen yardımcı fonksiyon.
 * Monotonic zaman kaynağı kullanarak sistem saati değişimlerinden etkilenmez.
 */
@Composable
fun rememberThrottledOnClick(
    throttleMs: Long = 300L,
    onClick: () -> Unit
): () -> Unit {
    require(throttleMs >= 0L) { "throttleMs must be >= 0" }

    val latestOnClick by rememberUpdatedState(onClick)
    val latestThrottleMs by rememberUpdatedState(throttleMs)

    var lastClickMark by remember { mutableStateOf<TimeMark?>(null) }

    return remember {
        {
            val last = lastClickMark
            val throttle = latestThrottleMs.milliseconds

            val allowed = (last == null) || (last.elapsedNow() >= throttle)
            if (allowed) {
                // Önce işaretle, sonra çalıştır: re-entrancy durumlarında da güvenli
                lastClickMark = TimeSource.Monotonic.markNow()
                latestOnClick()
            }
        }
    }
}