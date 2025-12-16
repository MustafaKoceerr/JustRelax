package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.data.dto.RemoteSoundDto
import com.mustafakoceerr.justrelax.core.data.mapper.SoundMapper
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.database.SoundEntity
import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SoundRepositoryImpl(
    private val db: JustRelaxDb,
    private val client: HttpClient,
    private val settingsRepository: SettingsRepository,
    private val mapper: SoundMapper,
    private val json: Json // <--- YENİ: Inject edildi
) : SoundRepository {
    private val CONFIG_URL = "https://pub-728a358af0b143fcbf9aa1e060e0dfa9.r2.dev/config.json"

    private val queries = db.justRelaxDbQueries

    /**
     * UI bu fonksiyonu dinler.
     * Best Practice: combine() operatörü kullandık.
     * Böylece kullanıcı DİLİ değiştirdiği an veya DB güncellendiği an
     * liste otomatik olarak yeni dilde ve yeni haliyle UI'a akar.
     */

    override fun getSounds(): Flow<List<Sound>> {
        val dbFlow = queries.getAllSounds()
            .asFlow()
            .mapToList(Dispatchers.IO)

        val languageFlow = settingsRepository.getLanguage()

        return combine(dbFlow, languageFlow) { entities, language ->
            entities.map { entity ->
                mapper.map(entity, language.code)
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSoundById(id: String): Sound? {
        return withContext(Dispatchers.IO) {
            val entity = queries.getSoundById(id).executeAsOneOrNull() ?: return@withContext null
            // DÜZELTME: Hardcoded "en" yerine, gerçek dili çekiyoruz.
            // first() fonksiyonu akıştan o anki değeri alır ve durur.
            val currentLang = settingsRepository.getLanguage().first().code
            mapper.map(entity, currentLang)
        }
    }

    /**
     * SENKRONİZASYON (Büyük Final)
     * 1. JSON'ı indir.
     * 2. Veritabanına yaz.
     * 3. Silinenleri temizle.
     */
    override suspend fun syncSounds() {
        withContext(Dispatchers.IO) {
            try {
                // 1. veriyi çek
                val responseBody = client.get(CONFIG_URL).body<String>()
                val remoteList = json.decodeFromString<List<RemoteSoundDto>>(responseBody)

                // 2. Transaction ile Güvenli Yazma // fail olursa hiç yazmaz. rollback atar
                queries.transaction {
                    // A. Gelenleri Ekle/Güncelle (Upsert)
                    remoteList.forEach { dto ->
                        // Map -> JSON String dönüşümü
                        val namesJsonStr = json.encodeToString(dto.names)
                        // Mevcut kaydı korumak için önce var mı diye bakabiliriz
                        // Ama SQL sorgumuz "INSERT OR REPLACE" olduğu için
                        // localPath verisini kaybetmemek adına önce eski veriyi çekmemiz lazım.

                        val existing = queries.getSoundById(dto.id).executeAsOneOrNull()
                        val currentLocalPath = existing?.localPath

                        queries.upsertSound(
                            id = dto.id,
                            category = dto.category,
                            nameJson = namesJsonStr,
                            iconUrl = dto.iconUrl,
                            audioUrl = dto.audioUrl,
                            localPath = currentLocalPath, // İndirilmişse yolu koru!
                            version = dto.version.toLong()
                        )
                    }
                    // B. Silinenleri Temizle (Remote'da yoksa Local'den sil)
                    // Tüm yerel ID'leri çek
                    val allLocalIds = queries.getAllSounds().executeAsList().map { it.id }
                    val allRemoteIds = remoteList.map { it.id }.toSet()

                    allLocalIds.forEach { localId ->
                        if (localId !in allRemoteIds) {
                            // TODO: İleride burada dosyayı da diskten sileceğiz.
                            queries.deleteSoundById(localId)
                        }
                    }
                }
            } catch (e: Exception) {
                // İnternet yoksa veya JSON bozuksa sessizce başarısız ol.
                // Kullanıcı eski verilerle devam eder (Offline-First).
                e.printStackTrace()
            }
        }
    }

    override fun getDownloadedSounds(): Flow<List<Sound>> {
        val dbFlow = queries.getDownloadedSounds()
            .asFlow()
            .mapToList(Dispatchers.IO)

        val languageFlow = settingsRepository.getLanguage()

        return combine(dbFlow, languageFlow) { queryResults, language ->
            queryResults.map { result ->
                // DÜZELTME: 'GetDownloadedSounds' tipini 'SoundEntity' tipine çeviriyoruz.
                // Mapper bizden SoundEntity bekliyor.
                val entity = SoundEntity(
                    id = result.id,
                    category = result.category,
                    nameJson = result.nameJson,
                    iconUrl = result.iconUrl,
                    audioUrl = result.audioUrl,
                    localPath = result.localPath,
                    version = result.version
                )

                mapper.map(entity, language.code)
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getMissingSounds(): List<Sound> {
        return withContext(Dispatchers.IO) {
            // SQLDelight sorgusu: WHERE localPath IS NULL
            val entities = queries.getMissingSounds().executeAsList()
            // Mapper ile çevir (Dil parametresi önemli değil, ID ve URL lazım)
            val currentLang = settingsRepository.getLanguage().first().code
            entities.map { mapper.map(it, currentLang) }
        }
    }

}