# Senin yapacakların

Claude'un tek başına yapamadığı işler, öncelik sırasıyla. Biten maddeyi silebilir ya da işaretleyebilirsin.

## 1. PR'ları birleştir
- [x] #36–#48 main'e birleştirildi (2026-10-18, main CI yeşil).

## Yeni tasarım: Mixer, AI ve Saved (PR #57 → #58 → #59)
- [ ] PR'lara göz at ve sırayla birleştir (#57, sonra #58, sonra #59).
- [ ] AI sonuç ekranını bir kez gör. Emülatörde yerel Worker OpenAI'dan 502 aldı; `server/api-proxy/.dev.vars`'taki `OPENAI_API_KEY` eski anahtar olabilir. Yeni anahtarı oraya yazarsan ben yerelde denerim; ya da Play'den kurulan sürümde bir öneriye dokunman yeter.
- [ ] İsteğe bağlı: telefon bağlıyken bana "profil" de. Baseline Profile'ı yeniden üretirim; eskisinde kaldırılan Lottie ekranının satırları var (zararsız, sadece eski).

## Tasarım 2. tur: tema, gezinme, ana sayfa, ayarlar, açılış (yığılı PR'lar, #59'un üstünde)
- [ ] PR'ları sırayla birleştir: theme-shell → home → settings → launch.
- [ ] Telefonda bir kez benchmark çalıştır: zamanlayıcı artık mini oynatıcıdan açılıyor, ses düzeyi de kartın üzerinde kaydırılarak ayarlanıyor. Benchmark adımları buna göre güncellendi ama henüz cihazda koşmadı.
- [ ] iOS uygulama simgesi hâlâ KMP şablonunun mavi küpü (`iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`). Android'deki yaprak simgesinin 1024 px sürümünü oraya koyman gerekiyor.

## Performans ölçümü
Telefonda yapıldı; sonuçlar `docs/perf/results.md`'de. Benchmark sürümü ayrı bir paket adıyla (`com.mustafakoceerr.justrelax.benchmark`) kuruluyor ve Play sürümüyle yan yana durabiliyor.
- [ ] Telefonundaki JustRelax'ı Play Store'dan yeniden kur (ilk denemede ana kullanıcıdan kaldırıldı).
- [ ] Ses kaydırıcısı düzeltmelerini (PR #54 ve `perf/engine-volume-path`) telefonda ölç. Telefonu USB ile bağla, *Stay awake* açık olsun, uygulamayı bir kez açıp bir sesin indiğinden emin ol, sonra bana "ölç" de. Şu komutu çalıştırıp sonucu `docs/perf/results.md`'ye yazarım:
  `ANDROID_SERIAL=R68R800REKZ ./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mustafakoceerr.justrelax.benchmark.LibraryBenchmark#dragVolume`
- [ ] İstersen benchmark uygulamalarını kaldır: `adb uninstall com.mustafakoceerr.justrelax.benchmark` ve `adb uninstall com.mustafakoceerr.justrelax.macrobenchmark`.

## 2. Play Integrity
- [x] Cloud projesi bağlı: JustRelax (`gen-lang-client-0305505013`, numara `266953836390`). Servis hesabı `justrelax-integrity` aynı projede, anahtarı Worker'da (2026-10-18).
- [x] `PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER=266953836390` `local.properties`'te.
- [ ] Cloud Console'da bu projede *APIs & Services → Enabled APIs* altında **Google Play Integrity API**'nin açık olduğunu bir kez kontrol et.

## 3. OpenAI
- [x] Yeni hesap/anahtar: yalnızca *Chat completions → Request*, `gpt-4o-mini`, 7 $ kredi, aylık 5 $ sert sınır. Anahtar Worker'da (2026-10-18).
- [x] "Share inputs and outputs" açık (ücretsiz günlük kota). Politika, kullanım koşulları, veri güvenliği notları ve uygulamadaki ilk kullanım onayı buna göre.
- [ ] Eski v1 anahtarının hesabı bulunamadı. Bulursan (Gmail'de `from:openai.com`, myaccount.google.com/connections) o anahtarı **Revoke** et.

## 4. Yayın (sen yapacaksın)
- [x] **Yükleme anahtarı:** yeni anahtar oluşturuldu, `.pem` Play Console'a gönderildi (2026-10-18). Sıfırlama Play'in onayından sonra geçerli olur (yaklaşık 2 gün).
- [x] `versionCode` 6, `versionName` 1.3.0 (2026-10-18, etiket `v1.3.0`).
- [ ] **İmzalı AAB'yi komutla al.** Şifreleri sen yaz; `local.properties` git'e girmez:
  ```
  RELEASE_STORE_FILE=/jks/dosyasının/tam/yolu.jks
  RELEASE_STORE_PASSWORD=
  RELEASE_KEY_ALIAS=upload
  # Anahtar şifresi store şifresinden farklıysa:
  # RELEASE_KEY_PASSWORD=
  ```
  Sonra `./gradlew :androidApp:bundleRelease` çalıştır. Çıktı: `androidApp/build/outputs/bundle/release/androidApp-release.aab`. Bu satırlar yoksa release imzasız çıkar; Android Studio'daki *Generate Signed App Bundle* sihirbazı da çalışmaya devam eder.
  İmzayı kontrol et: `keytool -printcert -jarfile androidApp/build/outputs/bundle/release/androidApp-release.aab` çıktısındaki SHA-256, Play Console → *Uygulama bütünlüğü → Play Uygulama İmzalama* sayfasındaki **yükleme anahtarı sertifikasının** SHA-256'sıyla aynı olmalı.
- [ ] Internal testing kanalına imzalı AAB yükle; Play'den kurulan sürümde AI karışımını ve Freesound aramasını dene. Integrity yalnızca Play'den kurulan uygulamada geçer.

## 5. Firebase (Crashlytics ve Analytics)
- [x] Proje `justrelax-app` açıldı (2026-10-18). İki Android uygulaması var: `com.mustafakoceerr.justrelax` ve benchmark için `com.mustafakoceerr.justrelax.benchmark`. `androidApp/google-services.json` yerinde ve git'e girmiyor. Emülatörde denendi: izin yokken hiçbir şey gitmiyor, izin verilince olaylar ve bir çökme raporu gönderiliyor.
- [x] Google Analytics bağlandı; DebugView'de olaylar görüldü (2026-10-18).
- [x] Saklama süresi: olay verisi 2 ay, kullanıcı verisi 14 ay (politikadaki "en fazla 14 ay" ile uyumlu).
- [ ] Yeni bir bilgisayarda ya da CI'da Firebase'li build gerekirse `google-services.json`'u konsoldan indir (*Project settings → Your apps*).

## 6. Play Console ve gizlilik
- [ ] Veri güvenliği formunu `docs/play-data-safety.md`'ye göre doldur. AI cümlesi satırında artık **Paylaşılıyor: Evet**.
- [x] Gizlilik politikası güncellendi ve 12 dilde yayında (2026-10-18). Kaynağı `docs/legal/`.
- [x] Politika OpenAI'ın veri paylaşımına göre güncellendi; kullanım koşulları yeniden yazıldı. İkisi de 12 dilde yayında, kaynakları `docs/legal/` (2026-10-18).

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
- [x] Main'e birleşmiş eski dallar GitHub'dan ve yerelden silindi (2026-10-19).
- [ ] İstersen GitHub → *Settings → General → Automatically delete head branches*'i aç; birleşen PR dalları kendiliğinden silinir.
