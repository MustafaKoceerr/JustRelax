# JustRelax v2 — Soundscape motoru ve uygulamanın yeniden inşası

> **Durum (2026-10-16):** Aşama 1–3 tamamlandı. Kalan aşamalar daha geniş bir yeniden inşa planına dağıtıldı:
> - Aşama 4 → Faz 5 (SoundPool olay player'ı)
> - Aşama 5 → Faz 4 (veri v2)
> - Aşama 6 → Faz 7a/7b (ekranlar), adaptive kabuktan (Faz 6) sonra
> - Aşama 7 → Faz 8 (~15 seslik yerleşik paket)
>
> Yeni plandaki öteki fazlar:
> - build-logic ve CocoaPods temizliği
> - NiA modül yapısı
> - Media3'te tek gerçek player (`MediaController` + `SceneRepository`)
> - Benim seslerim (içe aktarma, kayıt, kırpma)
> - Freesound

## Context
Kullanıcının asıl şikayetleri: uygulama yeterince işlevsel değil; AI hep aynı/alakasız sesleri seçiyor ("kamptayım" → sadece kamp ateşi). İnceleme sonucu kök nedenler:
- **AI sesleri tanımıyor.** `feature/ai/.../OpenAiRepositoryImpl.kt` modele sadece ID listesi gönderiyor; üstelik yalnızca indirilmiş sesleri (starter pack = 17 ses).
- **Mixer'ın ifade gücü kısıtlı.** Her ses sonsuz döngü. Kuş, baykuş, gök gürültüsü gibi "olay" sesleri aynı noktada tekrar ediyor; bu hem gerçekçiliği öldürüyor hem de tekrarı gizlemek için uzun (3 dk) ve büyük dosyalar gerektiriyor.
- Katalog: 76 ses, hepsi 256 kbps mono MP3, yaklaşık 100 MB. MP3 döngü noktasında boşluk bırakmaya yatkın.

Kullanıcıyla kararlaştırılan yön: oyun ses tasarımındaki yöntem.
- **Zemin (bed):** kısa, dikişsiz döngü (yağmur, rüzgâr, dere, gürültü).
- **Olay (event):** kısa kayıtlar, **rastgele aralık, ses ve stereo konumla** çalınır (kuş, baykuş, odun çatırtısı, gök gürültüsü).

Sonuç: hiç tekrar etmeyen canlı ortamlar ve çok daha küçük dosyalar. Kullanıcı "kır parçala, neredeyse baştan yaz" dedi.

**Ne kalıyor, ne yeniden yazılıyor?**
- **Altyapı kalıyor:** build-logic, `:androidApp`, Media3 servisi, kesinti politikası, Navigation 3, MVVM kalıbı, test altyapısı.
- **Yeniden yazılıyor:**
  - domain modeli (ses türleri, sahne/katman)
  - ses motoru
  - katalog formatı ve veritabanı şeması (yıkıcı, eski veri taşınmaz)
  - kayıtlı mix → kayıtlı sahne
  - feature ekranlarının ses/mix kısmı

**Kapsam dışı (sonraki ayrı planlar):** Benim seslerim, online kütüphane (Freesound), AI yönetmen. Ses seçimi ve içerik üretimi bu planın **en son** aşaması. Geliştirme sırasında birkaç sesten oluşan bir debug paketi kullanılır.

Çalışma şekli: her aşama TDD (önce test), aşama sonunda commit ve kısa gözden geçirme. Tek uzun branch `feature/soundscape-v2`, aşama başına bir PR.

---

## Aşama 1 — Domain modeli (saf Kotlin, `core:model` + `core:domain`)
- `SoundKind { BED, EVENT }`
- `Sound(id, names, category, kind, clips: List<SoundClip>, tags)`: BED tek klip, EVENT 1..n varyasyon klibi (ör. 3 farklı baykuş).
- `SoundClip(url, localPath?, durationMs, sizeBytes)`. Bir ses, tüm klipleri inmişse "indirilmiş" sayılır.
- `EventFrequency { RARE, NORMAL, OFTEN }`: aralıklar sırasıyla 40–120 sn, 15–45 sn, 5–15 sn. Değerler tek yerde sabit; dinleyerek ayarlanacak.
- `Scene(layers)`, `Layer(soundId, volume, frequency?)`: frequency yalnızca EVENT katmanlarında.
- Kurallar ve testler: katman limiti (toplam 10), aynı ses iki kez eklenemez, BED katmanında frequency olamaz.

## Aşama 2 — Olay zamanlayıcı (motorun kalbi, `core:audio` commonMain, TDD)
`EventScheduler(random: Random, oneShotPlayer)`: her EVENT katmanı için bir coroutine döngüsü.
- **Zamanlama:** `delay(frequency aralığında rastgele)` ile bekler.
- **Klip seçimi:** rastgele varyasyon seçer; aynı klip art arda gelmez.
- **Ses ve konum:** ses seviyesi katman sesi ±%15, stereo konum −0,6…+0,6.
- **Çalma:** `oneShotPlayer.play(clip, volume, pan)`.

Duraklatınca bekleme donar ve devam edince kaldığı yerden sürer. Katman kaldırılınca döngü iptal olur. Frequency değişirse bir sonraki beklemeden itibaren geçerli olur. Aynı anda en fazla N olay çalar.

Testler sanal zaman (`runTest`) ve sabit tohumlu `Random` ile:
- aralıklar sınırların içinde kalıyor
- art arda aynı klip gelmiyor
- volume/pan jitter sınırlarda kalıyor
- pause/resume beklemeyi koruyor
- katman kaldırınca olay kesiliyor
- eşzamanlılık sınırı aşılmıyor

## Aşama 3 — SoundscapeEngine (`AudioMixer`'ın yerine, commonMain, TDD)
- **Arayüz:**
  - `state: StateFlow<SoundscapeState(layers, isPlaying)>`
  - `play(scene)`, `addLayer`, `removeLayer`, `setVolume`, `setFrequency`, `pauseAll`, `resumeAll`, `stopAll`
- **Zeminler:** mevcut döngü player'ları (`SoundPlayer`, `ExoSoundPlayer`). Fade-in korunur; her zemin **rastgele bir noktadan başlar**, böylece aynı anda başlayan döngüler senkron kalmaz.
- **Olaylar:** `EventScheduler`.
- **Korunan dayanıklılık kuralları:** `DefaultAudioMixer`'ın yarış durumu kuralları ve testleri yeni motora taşınır (limit, hazırlanırken durdurma, kısmi hata, pause sırasında fade-in).
- **Uyarlanan bağlantılar:** `PlaybackInterruptionPolicy`, `MixerSessionPlayer` (bildirim/kilit ekranı), `TimerManagerImpl` (uyku zamanlayıcısı) ve `SoundController` yeni motora uyarlanır; mevcut testleri yeşil kalır.

## Aşama 4 — Android oynatıcıları ve debug ses paketi
- **`AndroidEventPlayer` (SoundPool):**
  - kısa olay klipleri için tasarlanmış, düşük gecikme
  - stereo konum sol/sağ ses ile verilir
  - medya `AudioAttributes` kullanır, focus tek noktadan alınmaya devam eder
  - katman eklenince klipleri önceden yükler
- **Format:** Ogg Opus. Dikişsiz döngü sağlar ve aynı kalitede MP3'ten çok daha küçüktür. ExoPlayer ve SoundPool minSdk 29'da destekler.
- **Debug paketi:** `ffmpeg` ile mevcut kataloğun birkaç sesinden kesilir; şimdilik sadece motoru kulakla test etmek için:
  - 2 zemin (ör. hafif rüzgâr, dere)
  - 2 olay (ör. kuş ötüşü 3 varyasyon, odun çatırtısı 2 varyasyon)
  - `androidApp/src/debug/assets/`'e konur
  - debug build'de Koin, uzak katalog yerine bu yerel kataloğu kullanır

  CDN'e dokunulmaz.
- **Robolectric:** SoundPool'a yükleme, pan → sol/sağ ses dönüşümü, servis yaşam döngüsü.
- **Kontrol noktası:** kullanıcı debug build'de "orman/kamp" sahnesini dinler; aralıklar ve jitter kulakla ayarlanır.

## Aşama 5 — Veri katmanı v2 (yıkıcı)
- **Katalog:** `sounds-v2.json` (kind, clips, tags). Network DTO ve mapper yeniden yazılır; MockEngine testleri.
- **Veritabanı şeması:**
  - `sound(id, names, category, kind, tags)`
  - `sound_clip(sound_id, idx, url, local_path, duration_ms, size_bytes)`
  - `saved_scene`, `saved_scene_layer(scene_id, sound_id, volume, frequency)`
  - Eski tablolar düşürülür (migration: drop + create). In-memory SQLite testleri.
- **İndirme:** ses başına tüm klipler (`DownloadSingleSoundUseCase` → klip bazlı). Mevcut batch/paralellik/temp-dosya mantığı ve testleri korunur.
- **Sync:** v2 katalog URL'si; yeni alanlar korunarak `INSERT`/`UPDATE`, cascade testleri.

## Aşama 6 — Ekranlar (MVVM kalıbı aynen)
- **Home (kütüphane):** kategori grid; dokununca sahneye katman ekler/çıkarır. Zemin kartında ses seviyesi; olay kartında ses seviyesi + **sıklık** (Seyrek / Normal / Sık).
- **Mini oynatıcı ve bildirim:** sahne katmanlarını gösterir, kaydet → "sahne" olarak kaydeder.
- **Mixer (rastgele):** 1–2 zemin + 1–3 olaydan rastgele sahne. Sayı seçici "katman sayısı" olur.
- **Saved:** kayıtlı sahneler; oynat, sil, geri al.
- **AI (geçici, minimal uyum):** derlenmeye ve çalışmaya devam eder. Modele ses türü/isim/kategori gönderilir, cevap sahneye çevrilir. Asıl AI yeniden tasarımı ayrı planda.
- **Timer, Settings, Onboarding:** yalnızca gereken uyarlamalar.

## Aşama 7 — İçerik (en son)
- Kullanıcıyla birlikte ~15 ses seçimi (zemin/olay dengesi). İçerik planı bu aşamada ayrıca yapılacak.
- `tools/sound-pipeline/` script'i: MP3 → Ogg Opus (zeminler ~64–96 kbps, döngü noktası çapraz geçişli), olay kliplerini kesme/normalize etme, `sounds-v2.json`'u üretme.
- Kullanıcı dosyaları Cloudflare'da `/v2/` altına yükler. Eski `/audio` ve `sounds.json` yerinde kalır.
- Release build v2 kataloğa geçer.

---

## Riskler / açık noktalar
- **SoundPool sınırları:** büyük klipleri belleğe açar; olay klipleri kısa (≤10 sn) tutulur. Gerekirse olaylar için de ExoPlayer'a geçilir; arayüz buna izin veriyor.
- **Opus döngü boşluğu:** ExoPlayer pre-skip'i işler. Aşama 4'te emülatörde bir döngü dinlenerek doğrulanır.
- **Bildirim/oturum:** `MixerSessionPlayer` motor durumundan beslendiği için arayüz değişse de davranış testleri korunur.

## Doğrulama
- **Her aşama:** `./gradlew testAndroidHostTest :androidApp:testDebugUnitTest` yeşil; R8 release build; iOS derlemesi (`-Pjustrelax.ios.enabled=true`).
- **Aşama 4 ve 6 sonu:** emülatörde debug paketiyle sahne çalma, olayların rastgele aralıkla duyulması (log + dinleme), arka plan/kilit ekranı/telefon araması senaryoları (mevcut script'ler) ve navigasyon senaryosu.
- **Aşama 7 sonu:** gerçek katalogla temiz kurulum, indirme ve offline çalma.
