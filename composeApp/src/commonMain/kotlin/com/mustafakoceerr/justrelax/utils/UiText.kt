package com.mustafakoceerr.justrelax.utils

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * ViewModel'den UI'a metin taşımak için kullanılan sarmalayıcı.
 * ViewModel'in Context veya Composable fonksiyon bilmesine gerek kalmaz.
 */

sealed interface UiText{
    // 1. Dinamik String (Backend'den gelen hata mesajları vs. için - Nadir kullanılır)
    data class DynamicString(val value: String): UiText

    // 2. Resource String (Bizim kullanacağımız asıl yapı)
    // args: String içinde parametre varsa (Örn: "%s kaydedildi") onları tutar.
    class StringResource(
        val resId: org.jetbrains.compose.resources.StringResource,
        vararg val args: Any
    ): UiText
}

/**
 * UI tarafında bu sınıfı gerçek String'e çeviren yardımcı fonksiyon.
 * Sadece Composable içinde çağrılabilir.
 */
@Composable
fun UiText.asString(): String{
    return when(this){
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(resId, *args)    }
}

/**
 * YENİ EKLENEN: Coroutine (LaunchedEffect, ViewModel) içinde kullanım için.
 * Örn: Snackbar, Toast, Logging
 */
suspend fun UiText.asStringSuspend(): String{
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> getString(resId, *args) // 'getString' suspend fonksiyondur
    }
}