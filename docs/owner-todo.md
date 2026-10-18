# Senin yapacakların

Claude'un tek başına yapamadığı işler, öncelik sırasıyla. Biten maddeyi silebilir ya da işaretleyebilirsin.

## 1. PR'ları birleştir
- [x] #36–#48 main'e birleştirildi (2026-10-18, main CI yeşil).

## Performans ölçümü
Telefonda yapıldı; sonuçlar `docs/perf/results.md`'de. Benchmark sürümü ayrı bir paket adıyla (`com.mustafakoceerr.justrelax.benchmark`) kuruluyor ve Play sürümüyle yan yana durabiliyor.
- [ ] Telefonundaki JustRelax'ı Play Store'dan yeniden kur (ilk denemede ana kullanıcıdan kaldırıldı).
- [ ] İstersen benchmark uygulamalarını kaldır: `adb uninstall com.mustafakoceerr.justrelax.benchmark` ve `adb uninstall com.mustafakoceerr.justrelax.macrobenchmark`.

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
- [x] Gizlilik politikası güncellendi ve 12 dilde yayında (2026-10-18). Kaynağı `docs/legal/`.
- [ ] İstersen kullanım koşullarını (`terms-conditions.html`) da güncelletebilirsin; hâlâ yalnızca OpenAI'dan söz ediyor.
- [ ] Firebase'de Analytics saklama süresini seçerken politikada "en fazla 14 ay" yazdığını unutma (2 ya da 14 ay, ikisi de uyuyor).

## 7. İçerik
- [ ] 15 yerleşik sesin kaynak lisanslarını doğrula (`tools/sound-pipeline/manifest.json`'daki `license_note`). Şu an `OWN` olarak yayında.
- [ ] Son ses seçimini yap. Pipeline, yükleme ve önbellek temizleme adımları `tools/sound-pipeline/README.md`'de.
- [ ] Kendi seslerimden kaydedilmiş bir döngüde başa dönüşü dinle. AAC'de boşluk ya da tık varsa söyle; yedek çözüm hazır.

## iOS sesi (#47)
Kod ve simülatör testleri hazır; kulakla dinlemek senin işin. Simülatörde ya da iPhone'da (iOS 18.4+):
- [ ] Bir yağmur ve bir olay sesi (kuşlar, çan) çal: ikisi de duyuluyor, olaylar sağda solda rastgele çıkıyor.
- [ ] Döngünün başa dönüşünde boşluk ya da tık var mı?
- [ ] Kilit ekranında ve Denetim Merkezi'nde JustRelax görünüyor; oynat/duraklat çalışıyor.
- [ ] Arama gelince ses duruyor, kapanınca devam ediyor; kulaklığı çıkarınca duruyor.
- [ ] Başka bir uygulama (Müzik) çalmaya başlayınca JustRelax susuyor.

## Bahşiş (#48)
Ekran ve mimari hazır; debug sürümünde örnek fiyatlarla görünüyor, yayın sürümünde gizli. Gerçek ödeme için:
- [ ] Play Console → *Monetize → Products → In-app products*: üç tüketilebilir (consumable) ürün aç: `tip_small`, `tip_medium`, `tip_large`. Fiyatları sen seç.
- [ ] Play Console'da ödeme profili ve banka hesabı (Payments profile).
- [ ] App Store Connect'te aynı id'lerle üç *Consumable* ürün (iOS yayınına doğru).
- [ ] Hazır olunca söyle: Play Billing ve StoreKit'i `TipRepository`'nin arkasına bağlarım; satır o zaman yayında da görünür.

## 8. İsteğe bağlı / sonra
- [ ] iOS: App Attest (AI ve Freesound için) ve Firebase iOS, iOS yayınına doğru.
- [ ] GitHub'daki eski 12 dal (`hotfix`, `improve-ui`, `Multimodules` vb.): istersen silinebilir.
