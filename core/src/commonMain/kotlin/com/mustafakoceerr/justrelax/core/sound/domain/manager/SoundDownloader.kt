package com.mustafakoceerr.justrelax.core.sound.domain.manager

interface SoundDownloader{
    /**
     * Sesi indirmeye başlar.
     * @param soundId: İndirilecek sesin ID'si (DB'den URL'i bulur)
     * @return Başarılı olursa true, hata olursa false döner.
     */
    suspend fun downloadSound(soundId: String): Boolean

    // İleride buraya 'getProgress(soundId): Flow<Float>' ekleyebiliriz.
    // TODO: Ekleme yap ve ui'da da göster.
}