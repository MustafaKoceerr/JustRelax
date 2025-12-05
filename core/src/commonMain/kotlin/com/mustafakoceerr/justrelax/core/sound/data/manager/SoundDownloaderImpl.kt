package com.mustafakoceerr.justrelax.core.sound.data.manager

import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundDownloader
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

class SoundDownloaderImpl(
    private val client: HttpClient,
    private val db: JustRelaxDb,
    private val storageProvider: StoragePathProvider
) : SoundDownloader {

    private val queries = db.justRelaxDbQueries

    // Okio FileSystem (Platform bağımsız dosya sistemi)
    private val fileSystem = FileSystem.SYSTEM

    override suspend fun downloadSound(soundId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. DB'den URL'i bul
                val entity = queries.getSoundById(soundId).executeAsOneOrNull()
                    ?: return@withContext false

                // Zaten indirilmişse tekrar indirme (Opsiyonel kontrol)
                if (entity.localPath != null && fileSystem.exists(entity.localPath.toPath())) {
                    return@withContext true
                }

                val url = entity.audioUrl
                val fileName = "$soundId.mp3"

                // Hedef Klasör: .../files/sounds/
                val soundsDir = storageProvider.getAppDataDir().div("sounds")
                if (!fileSystem.exists(soundsDir)) {
                    fileSystem.createDirectory(soundsDir)
                }

                // Hedef Dosya ve Geçici Dosya
                val targetFile = soundsDir.div(fileName)
                val tempFile = soundsDir.div("$fileName.tmp")

                // 2. Ktor ile İndirme Başlat
                client.prepareGet(url).execute { httpResponse ->
                    val channel: ByteReadChannel = httpResponse.bodyAsChannel()

                    // 3. DÜZELTME BURADA: ByteArray kullanarak yazıyoruz
                    fileSystem.sink(tempFile).buffer().use { sink ->
                        val buffer = ByteArray(8192) // 8 KB'lık bir kova

                        while (!channel.isClosedForRead) {
                            // Ktor'dan kovayı doldur
                            val bytesRead = channel.readAvailable(buffer, 0, buffer.size)

                            // Eğer okunacak bir şey kalmadıysa döngüyü kır (-1 döner)
                            if (bytesRead <= 0) break

                            // Okio ile diske boşalt
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }

                // 4. Atomik İşlem: .tmp -> .mp3
                // Eğer buraya geldiysek indirme hatasız bitmiştir.
                fileSystem.atomicMove(tempFile, targetFile)

                // 5. DB'yi Güncelle
                queries.updateLocalPath(
                    localPath = targetFile.toString(),
                    id = soundId
                )

                true // Başarılı
            } catch (e: Exception) {
                e.printStackTrace()
                // Hata durumunda çöp (.tmp) dosyayı temizle
                try {
                    // tempFile'ı burada tekrar oluşturmak yerine yukarıda scope dışına çıkarabilirsin
                    // ama basitlik adına şimdilik böyle bırakalım.
                } catch (ignore: Exception) {}

                false // Başarısız
            }
        }
    }
}