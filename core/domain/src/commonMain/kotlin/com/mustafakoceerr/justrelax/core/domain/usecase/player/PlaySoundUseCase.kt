package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AppError
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
     * @param soundId Çalınacak sesin ID'si.
     * @param volume Opsiyonel ses seviyesi. Belirtilmezse varsayılan (0.5f) veya config kullanılır.
     * @param fadeInMs Opsiyonel fade-in süresi. Belirtilmezse SoundConfig varsayılanı (800ms) kullanılır.
     */
    suspend operator fun invoke(
        soundId: String,
        volume: Float? = null,
        fadeInMs: Long? = null
    ): Resource<Unit> {
        return try {
            // 1. Validasyon: ID boş mu?
            if (soundId.isBlank()) {
                return Resource.Error(AppError.Unknown(IllegalArgumentException("Sound ID cannot be empty")))
            }

            // 2. Veriyi Getir: Repository'den ses detayını al.
            // Flow kullandığımız için o anki güncel veriyi almak adına .first() kullanıyoruz.
            val sound = soundRepository.getSound(soundId).first()
                ?: return Resource.Error(AppError.Storage.FileNotFound())

            // 3. Kontrol: Ses indirilmiş mi? (Local Path var mı?)
            val localPath = sound.localPath
            if (localPath.isNullOrBlank()) {
                // Eğer indirilmemişse çalınamaz.
                // Not: Otomatik indirme başlatmak bu UseCase'in sorumluluğu DEĞİLDİR.
                // O işi UI, DownloadUseCase'i çağırarak yapmalıdır.
                return Resource.Error(AppError.Storage.FileNotFound())
            }

            // 4. Konfigürasyon: AudioMixer için gerekli paketi hazırla.
            // Eğer parametre olarak özel değerler (volume, fade) gelmişse onları kullan,
            // yoksa SoundConfig veri sınıfındaki default değerler (0.5f, 800ms) devreye girer.
            val config = SoundConfig(
                id = sound.id,
                url = localPath,
                initialVolume = volume
                    ?: 0.5f, // SoundConfig default'u ile aynı, burayı parametrik yaptık.
                fadeInDurationMs = fadeInMs ?: 800L,
                fadeOutDurationMs = 500L // Standart fade-out
            )

            // 5. Aksiyon: Mixer'a ilet.
            // Mixer hata fırlatırsa (örn: Limit doldu), aşağıdaki catch bloğuna düşer.
            audioMixer.playSound(config)

            Resource.Success(Unit)

        } catch (e: AppError) {
            // Zaten bizim tanımladığımız bir hataysa direkt döndür.
            Resource.Error(e)
        } catch (e: Exception) {
            // Beklenmeyen bir hataysa Unknown olarak paketle.
            Resource.Error(AppError.Unknown(e))
        }
    }
}