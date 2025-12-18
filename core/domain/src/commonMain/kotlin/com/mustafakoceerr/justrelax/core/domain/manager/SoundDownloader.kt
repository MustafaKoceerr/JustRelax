package com.mustafakoceerr.justrelax.core.domain.manager

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface SoundDownloader{
    /**
     * Sesi indirir ve durumunu akış olarak bildirir.
     * @param soundId: İndirilecek sesin ID'si
     */
    fun downloadSoundFlow(soundId: String): Flow<DownloadStatus>

    // Eski fonksiyonu (tekil indirme için) tutabiliriz veya Flow'a çevirebiliriz.
    // Kolaylık olsun diye eski fonksiyonu Flow'u collect edecek şekilde güncelleyebiliriz.
    suspend fun downloadSound(soundId: String): Boolean

}