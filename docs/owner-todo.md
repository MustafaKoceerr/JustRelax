# Senin yapacakların

Claude'un tek başına yapamadığı işler, öncelik sırasıyla. Biten maddeyi silebilir ya da işaretleyebilirsin.

## 1. PR'ları birleştir
PR'lar birbirinin üstüne yığılı; bu sırayla birleştir. Her biri bir öncekinin dalını temel alır, birleştirince GitHub sonrakinin tabanını kendisi günceller.
- [ ] #36 Anahtar proxy'si, Play Integrity, yalnızca ayarların yedeklenmesi
- [ ] #37 Mikrofon kaydının kaldırılması, bu liste
- [ ] #38 Crashlytics ve Analytics (ilk açılışta izin)
- [ ] #39 Mixer'daki çift snackbar ve karanlık temadaki beyaz pencere
- [ ] #40 Build temizliği: ortak convention'lar, katalog, CI'a proxy ve pipeline testleri
- [ ] #41 Üretim kodundaki tekrarlar ve ölü kod
- [ ] #42 Test tekrarları ve eksik testler
- [ ] #43 README

## 2. Play Integrity (AI ve Freesound bunsuz yayında çalışmaz)
- [ ] Play Console → JustRelax → *Test and release → App integrity → Play Integrity API → Link a Cloud project* ile yeni bir proje oluştur. Ücretsiz, faturalandırma gerekmez.
- [ ] Google Cloud Console'da o projede *IAM & Admin → Service accounts → Create*; rol verme. *Keys → Add key → JSON* ile anahtarı indir.
- [ ] Anahtarı Worker'a gir:
  ```
  ! cd server/api-proxy && npx wrangler secret put GOOGLE_SERVICE_ACCOUNT_JSON < ~/Downloads/<dosya>.json
  ```
- [ ] Projenin numarasını `local.properties`'e ekle: `PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER=...`

## 3. OpenAI (şu an hiç anahtar yok, AI özelliği çalışmıyor)
- [ ] Anahtarı Worker'a gir:
  ```
  ! cd server/api-proxy && npx wrangler secret put OPENAI_API_KEY
  ```
- [ ] OpenAI panelinde *Settings → Limits* ile aylık bütçe sınırı koy (örneğin 5 $).
- [ ] Yayınlanmış v1 sürümünde bir OpenAI anahtarı varsa onu iptal et.

## 4. Dahili test
- [ ] Internal testing kanalına bir sürüm yükle.
- [ ] Play'den kurulan sürümde AI karışımı ve Freesound aramasını dene. Integrity yalnızca Play'den kurulan uygulamada geçer.

## 5. Firebase (Crashlytics ve Analytics)
- [ ] Bir kez `! firebase login` çalıştır; Firebase projesini ve Android uygulamasını Firebase MCP ile ben oluştururum. Ya da `google-services.json`'u kendin `androidApp/` klasörüne koy.
- [ ] Firebase konsolunda Analytics veri saklama süresini seç (*Project settings → Data settings*).

## 6. Play Console ve gizlilik
- [ ] Veri güvenliği formunu `docs/play-data-safety.md`'ye göre doldur.
- [ ] Gizlilik politikasını güncelle (`cdn.mustafakoceerr.com/legal/<dil>/privacy-policy.html`). Neyin anlatılması gerektiği aynı dosyanın sonunda.

## 7. İçerik
- [ ] 15 yerleşik sesin kaynak lisanslarını doğrula (`tools/sound-pipeline/manifest.json`'daki `license_note`). Şu an `OWN` olarak yayında.
- [ ] Son ses seçimini yap. Pipeline, yükleme ve önbellek temizleme adımları `tools/sound-pipeline/README.md`'de.
- [ ] Kendi seslerimden kaydedilmiş bir döngüde başa dönüşü dinle. AAC'de boşluk ya da tık varsa söyle; yedek çözüm hazır.

## 8. İsteğe bağlı / sonra
- [ ] iOS: App Attest (AI ve Freesound için) ve Firebase iOS, iOS yayınına doğru.
- [ ] GitHub'daki eski 12 dal (`hotfix`, `improve-ui`, `Multimodules` vb.): istersen silinebilir.
