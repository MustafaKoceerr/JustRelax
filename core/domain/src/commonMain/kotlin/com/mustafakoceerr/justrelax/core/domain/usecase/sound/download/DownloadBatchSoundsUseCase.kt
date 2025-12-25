package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

/*
Bu sınıfın görevi:
Eline verilen ses listesini alır.
Semaphore (Trafik Polisi) kullanarak aynı anda en fazla 3 tanesini indirir.
Diğerlerini Queued (Sırada) bekletir.
Dışarıya sadece basit bir İlerleme Durumu (Örn: %30, %50, Tamamlandı) raporlar.
 */

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class DownloadBatchSoundsUseCase(
    private val downloadSingleSoundUseCase: DownloadSingleSoundUseCase
) {
    // 3 şeritli yol: Aynı anda sadece 3 indirme geçebilir.
    private val semaphore = Semaphore(3)

    operator fun invoke(sounds: List<Sound>): Flow<DownloadStatus> {
        val totalCount = sounds.size

        // İndirilecek dosya yoksa direkt bitir.
        if (totalCount == 0) return flowOf(DownloadStatus.Completed)

        return channelFlow {
            // Başlangıç sinyali (0 dosya bitti)
            send(0)

            sounds.forEach { sound ->
                launch {
                    // Trafik Polisi: İzin al (Eğer 3 kişi içerideyse kapıda bekle)
                    semaphore.withPermit {
                        try {
                            // Tekli indirmeyi çağır (İşçiye işi ver)
                            downloadSingleSoundUseCase(sound)
                        } catch (e: Exception) {
                            // Hata olsa bile akışı bozma, logla devam et.
                            e.printStackTrace()
                        } finally {
                            // İşlem bitti (Başarılı veya Hatalı), sayacı artır
                            send(1)
                        }
                    }
                }
            }
        }
            // Gelen "1" sinyallerini topla: 0 -> 1 -> 2 -> ... -> Total
            .runningFold(0) { finishedCount, signal ->
                finishedCount + signal
            }
            // Sayıyı Yüzdeye çevir
            .map { finishedCount ->
                val progress = finishedCount.toFloat() / totalCount

                if (progress >= 1.0f) {
                    DownloadStatus.Completed
                } else {
                    DownloadStatus.Progress(progress)
                }
            }
            // Gereksiz UI güncellemelerini engelle (Örn: %50 üst üste gelirse)
            .distinctUntilChanged()
    }
}