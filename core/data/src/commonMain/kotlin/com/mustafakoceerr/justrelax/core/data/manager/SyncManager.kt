package com.mustafakoceerr.justrelax.core.data.manager

import com.mustafakoceerr.justrelax.core.domain.manager.AppInitializer
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SyncManager(
    private val dataSeeder: DataSeeder,
    private val soundRepository: SoundRepository
): AppInitializer {
    /**
     * Uygulama açılışında çağrılacak tek fonksiyon.
     * Sıralama önemlidir: Önce yerel veriyi garantiye al, sonra internete bak.
     */
    override suspend fun initializeApp() {
        withContext(Dispatchers.IO){
            try {
                dataSeeder.seedData()
            }catch (e: Exception){
                e.printStackTrace()
            }

            try {
                soundRepository.syncSounds()
            }catch (e: Exception){
                // Hata fırlat ki AppInitializationManager yakalasın.
                throw e
            }
        }
    }
}