package com.mustafakoceerr.justrelax.core.data.manager

import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
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
        withContext(Dispatchers.IO) {
            // 1. ADIM: SEEDING (Yerel Dosyalar)
            // Burası internetten bağımsızdır, asla patlamamalı.
            try {
                dataSeeder.seedData()
            } catch (e: Exception) {
                e.printStackTrace()
                // Seeding hatası Sync'i durdurmasın.
            }

            // 2. ADIM: SYNC (Uzak Sunucu)
            // Burası SSL hatası verebilir, internet olmayabilir.
            try {
                soundRepository.syncSounds()
            } catch (e: Exception) {
                // e.printStackTrace() // Log kirliliği yapmasın diye kapattım, istersen aç.
            }
        }
    }
}