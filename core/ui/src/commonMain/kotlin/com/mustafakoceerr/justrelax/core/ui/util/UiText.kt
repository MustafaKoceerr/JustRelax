package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * ViewModel'den UI'a metin taşımak için kullanılan sarmalayıcı.
 * ViewModel'in Context veya Composable fonksiyon bilmesine gerek kalmaz.
 */
// Bu mühürlü arayüz sayesinde ViewModel, Context'e ihtiyaç duymadan mesaj taşıyabilir.
sealed interface UiText {
    // 1. Düz metin (Örn: API'den gelen dinamik hata mesajı)
    data class DynamicString(val value: String) : UiText

    // 2. Resource ID (Örn: strings.xml içindeki çevrilebilir metin)
    // formatArgs: "%s silindi" gibi dinamik alanlar için
    data class Resource(
        val resId: StringResource,
        val formatArgs: List<Any> = emptyList()
    ) : UiText

    // UI tarafında bu fonksiyonu çağırıp String'e çevireceğiz
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is Resource -> stringResource(resId, *formatArgs.toTypedArray())
        }
    }

    // LaunchedEffect veya ViewModel içinde (suspend) kullanmak için
    suspend fun resolve(): String {
        return when (this) {
            is DynamicString -> value
            is Resource -> {
                // Eğer argüman yoksa düz getString çağır, varsa formatlı çağır.
                if (formatArgs.isEmpty()) {
                    getString(resId)
                } else {
                    getString(resId, *formatArgs.toTypedArray())
                }
            }
        }
    }
}