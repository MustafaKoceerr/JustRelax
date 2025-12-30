package com.mustafakoceerr.justrelax.core.common

/**
 * Uygulama genelindeki ses sabitleri.
 * core:common modülünde olduğu için her yerden erişilebilir.
 */
object AudioDefaults {
    // Varsayılan ses seviyesi (%50)
    const val BASE_VOLUME = 0.5f

    // Varsayılan fade-in süresi (1 saniye)
    const val FADE_IN_MS = 1000L

    // Aynı anda çalabilecek maksimum ses sayısı
    const val MAX_CONCURRENT_SOUNDS = 10
}