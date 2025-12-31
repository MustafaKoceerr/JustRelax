package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import kotlinx.coroutines.flow.first

/**
 * Sorumluluk (SRP): Belirli bir sesin çalınma sürecini yönetmek.
 *
 * Bu UseCase şunları yapar:
 * 1. Sesin veritabanında var olduğunu doğrular.
 * 2. Sesin indirilmiş olduğunu (Local Path) doğrular.
 * 3. Gerekli konfigürasyonu (SoundConfig) hazırlar.
 * 4. AudioMixer'a "Çal" emrini verir.
 * 5. Olası hataları yakalayıp UI'a raporlar.
 */
class PlaySoundUseCase(
    private val soundRepository: SoundRepository,
    private val audioMixer: AudioMixer
) {

    /**
     * @param soundId Çalınacak sesin kimliği.
     * @param initialVolume Opsiyonel başlangıç ses seviyesi. Belirtilmezse varsayılan kullanılır.
     * @return Başarılıysa Resource.Success, hata varsa Resource.Error döner.
     */
    suspend operator fun invoke(
        soundId: String,
        initialVolume: Float = AudioDefaults.BASE_VOLUME
    ): Resource<Unit> {

        // 1. Veriyi Getir: Repository'den ses detayını al.
        val sound = soundRepository.getSound(soundId).first()
            ?: return Resource.Error(AppError.Storage.FileNotFound())

        // 2. Kontrol: Ses indirilmiş mi? (Local Path var mı?)
        val localPath = sound.localPath
        if (localPath.isNullOrBlank()) {
            return Resource.Error(AppError.Storage.FileNotFound())
        }

        // 3. Konfigürasyon: AudioMixer için gerekli paketi hazırla.
        val config = SoundConfig(
            id = sound.id,
            url = localPath,
            initialVolume = initialVolume.coerceIn(0f, 1f), // Gelen değeri güvenli aralığa çek
            fadeInDurationMs = AudioDefaults.FADE_IN_MS
        )

        // 4. Aksiyon: Mixer'a ilet.
        // Artık try-catch'e gerek yok, çünkü playSound() zaten Resource dönüyor.
        return audioMixer.playSound(config)
    }
}