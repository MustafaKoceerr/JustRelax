package com.mustafakoceerr.justrelax.core.data.manager

import com.mustafakoceerr.justrelax.core.data.dto.RemoteSoundDto
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.domain.manager.AssetReader
import com.mustafakoceerr.justrelax.core.domain.manager.StoragePathProvider
import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.SYSTEM
import okio.buffer
import okio.use

class DataSeeder (
    private val assetReader: AssetReader,
    private val storageProvider: StoragePathProvider,
    private val db: JustRelaxDb,
    private val settingsRepository: SettingsRepository,
    private val json: Json
){
    private val queries = db.justRelaxDbQueries
    private val fileSystem = FileSystem.Companion.SYSTEM

    suspend fun seedData(){
        withContext(Dispatchers.IO) {
            // 1. Kontrol: Daha önce yapıldıysa tekrar yapma
            // Kontrol
            if (settingsRepository.isInitialSeedingDone()) {
                return@withContext
            }

            try {
                // 1. Config Oku
                val configBytes = assetReader.readAsset("initial_config.json")
                val configString = configBytes.decodeToString()
                val initialSounds = json.decodeFromString<List<RemoteSoundDto>>(configString)

                val soundsDir = storageProvider.getAppDataDir().div("sounds")
                if (!fileSystem.exists(soundsDir)) {
                    fileSystem.createDirectory(soundsDir)
                }

                queries.transaction {
                    initialSounds.forEach { dto ->
                        try {
                            val assetFileName = dto.audioUrl.substringAfterLast("/")

                            // Dosyayı Asset'ten oku
                            val audioBytes = assetReader.readAsset(assetFileName)

                            // Diske yaz
                            val targetFile = soundsDir.div(assetFileName)
                            fileSystem.sink(targetFile).buffer().use { sink ->
                                sink.write(audioBytes)
                            }

                            // DB'ye yaz
                            val namesJsonStr = json.encodeToString(dto.names)
                            queries.upsertSound(
                                id = dto.id,
                                category = dto.category,
                                nameJson = namesJsonStr,
                                iconUrl = dto.iconUrl,
                                audioUrl = dto.audioUrl,
                                localPath = targetFile.toString(),
                                version = dto.version.toLong()
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                settingsRepository.setInitialSeedingDone(true)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}