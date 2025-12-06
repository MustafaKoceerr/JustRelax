package com.mustafakoceerr.justrelax.core.sound.domain.manager

import com.mustafakoceerr.justrelax.core.sound.data.manager.DataSeeder
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SyncManager(
    private val dataSeeder: DataSeeder,
    private val soundRepository: SoundRepository
) {
    /**
     * Uygulama açılışında çağrılacak tek fonksiyon.
     * Sıralama önemlidir: Önce yerel veriyi garantiye al, sonra internete bak.
     */
    suspend fun initializeApp(){
        withContext(Dispatchers.IO){
            // 1. ADIM: SEEDING (Yerel Dosyalar)
            // Burası internetten bağımsızdır, asla patlamamalı.
            try {
                println("SyncManager: Seeding başlatılıyor...")
                dataSeeder.seedData()
                println("SyncManager: Seeding tamamlandı.")
            } catch (e: Exception) {
                println("SyncManager: SEEDING HATASI!")
                e.printStackTrace()
                // Seeding hatası Sync'i durdurmasın.
            }

            // 2. ADIM: SYNC (Uzak Sunucu)
            // Burası SSL hatası verebilir, internet olmayabilir.
            try {
                println("SyncManager: Remote Sync başlatılıyor...")
                soundRepository.syncSounds()
                println("SyncManager: Remote Sync tamamlandı.")
            } catch (e: Exception) {
                println("SyncManager: REMOTE SYNC HATASI (İnternet yok veya SSL sorunu)")
                // e.printStackTrace() // Log kirliliği yapmasın diye kapattım, istersen aç.
                println("Hata Detayı: ${e.message}")
            }
        }
    }
}