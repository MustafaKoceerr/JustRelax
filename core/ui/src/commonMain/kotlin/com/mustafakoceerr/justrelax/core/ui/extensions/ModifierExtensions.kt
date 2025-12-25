package com.mustafakoceerr.justrelax.core.ui.extensions

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Bir onClick lambdasını, belirli bir süre boyunca tekrar çağrılmasını
 * engelleyecek şekilde sarmalayan bir Composable helper.
 *
 * @param debounceMs Tıklamalar arasındaki minimum bekleme süresi (milisaniye).
 * @param onClick Debounce korumasıyla çalıştırılacak olan asıl eylem.
 * @return Debounce mantığı eklenmiş yeni bir `() -> Unit` lambdası.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun rememberDebouncedOnClick(
    debounceMs: Long = 300L, // Varsayılan 1 saniye
    onClick: () -> Unit
): () -> Unit {
    val lastClickTime = remember { mutableLongStateOf(0L) }

    // Bu, Button'a verilecek olan yeni lambda
    return {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - lastClickTime.longValue > debounceMs) {
            lastClickTime.longValue = currentTime
            onClick()
        }
    }
}