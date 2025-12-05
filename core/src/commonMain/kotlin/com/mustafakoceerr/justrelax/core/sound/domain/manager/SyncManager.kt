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
            try {
                // 1. Adım: Seeding (Hızlı - Diskten okur)
                // Eğer daha önce yapıldıysa zaten hemen döner (return).
                dataSeeder.seedData()

                // 2. Adım: Sync (Yavaş - İnternetten çeker)
                // Bu işlem başarısız olsa bile (internet yoksa),
                // 1. adım sayesinde kullanıcının elinde veriler vardır.
                soundRepository.syncSounds()
            }catch (e: Exception){
                // Loglama yapılabilir (Crashlytics vs.)
                e.printStackTrace()
            }
        }
    }
}