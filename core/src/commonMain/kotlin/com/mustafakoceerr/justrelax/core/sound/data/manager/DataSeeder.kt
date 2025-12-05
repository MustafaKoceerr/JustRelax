package com.mustafakoceerr.justrelax.core.sound.data.manager

import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider
import com.mustafakoceerr.justrelax.core.seeding.AssetReader
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.sound.data.dto.RemoteSoundDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.SYSTEM
import okio.buffer
import okio.use

//Bu sınıf, assets klasöründeki dosyaları okuyup, Okio ile diske yazar ve veritabanını günceller.

class DataSeeder (
    private val assetReader: AssetReader,
    private val storageProvider: StoragePathProvider,
    private val db: JustRelaxDb,
    private val settingsRepository: SettingsRepository,
    private val json: Json
){
    private val queries = db.justRelaxDbQueries
    private val fileSystem = FileSystem.SYSTEM

    suspend fun seedData(){
        withContext(Dispatchers.IO){
            // 1. Kontrol: Daha önce yapıldıysa tekrar yapma
            if(settingsRepository.isInitialSeedingDone()) return@withContext

            try {
                // 2. Config dosyasını Asset'ten oku
                // Dosya adı: "initial_config.json" (Bunu assets klasörüne koymalısın)
                val configBytes = assetReader.readAsset("initial_config.json")
                val configString = configBytes.decodeToString()
                val initialSounds = json.decodeFromString<List<RemoteSoundDto>>(configString)

                val soundsDir = storageProvider.getAppDataDir().div("sounds")
                if (!fileSystem.exists(soundsDir)){
                    fileSystem.createDirectory(soundsDir)
                }

                queries.transaction{
                    initialSounds.forEach { dto->
                        try {
                            // 3. Ses dosyasını Asset'ten oku
                            // Asset'teki isim ile ID aynı olmalı veya JSON'da belirtilmeli.
                            // Varsayım: Asset adı = "id.mp3" (örn: rain.mp3)
                            val assetFileName = "${dto.id}.mp3"
                            val audioBytes = assetReader.readAsset(assetFileName)

                            // 4. Diske Yaz (Okio)
                            val targetFile = soundsDir.div(assetFileName)

                            // FileSystem.sink ile yazıyoruz
                            fileSystem.sink(targetFile).buffer().use { sink->
                                sink.write(audioBytes)
                            }
                            // 5. Veritabanına Kaydet (localPath dolu olarak!)
                            val namesJsonStr = json.encodeToString(dto.names)

                            queries.upsertSound(
                                id = dto.id,
                                category = dto.category,
                                nameJson = namesJsonStr,
                                iconUrl = dto.iconUrl,
                                audioUrl = dto.audioUrl,
                                localPath = targetFile.toString(), // <-- İŞTE SİHİR BURADA
                                version = dto.version.toLong()
                            )
                        }catch (e: Exception){
                            println("Seeding Error for ${dto.id}: ${e.message}")
                            // Bir dosya hatalıysa diğerlerine devam et (Transaction içinde olduğumuz için
                            // buradaki try-catch transaction'ı bozmaz, sadece o dosyayı atlar)
                        }
                    }
                }
                // 6. İşlem Başarılı: Bayrağı dik
                settingsRepository.setInitialSeedingDone(true)
            }catch (e: Exception){
                e.printStackTrace()
                // Genel bir hata olursa (örn: config.json yoksa) hiçbir şey yapma,
                // bir sonraki açılışta tekrar dener.
            }
        }
    }
}