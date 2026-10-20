# Senin yapacakların

Claude'un tek başına yapamadığı işler, öncelik sırasıyla. Biten maddeyi silebilir ya da işaretleyebilirsin.

## 1. PR'ları birleştir
- [x] #36–#48 main'e birleştirildi (2026-10-18, main CI yeşil).

## Yeni tasarım: Mixer, AI ve Saved (PR #57 → #58 → #59)
- [ ] PR'lara göz at ve sırayla birleştir (#57, sonra #58, sonra #59).
- [x] AI sonuç ekranı telefonda yerel Worker'la görüldü (2026-10-19). Debug sürüm `localhost:8787`'ye gider: `npm run dev` + `adb reverse tcp:8787 tcp:8787`, `.dev.vars`'ta `OPENAI_API_KEY` dolu olmalı.
- [x] AI ve Freesound (ara → dinle → ekle → editör → kaydet → My sounds) yerel Worker'la Android emülatörde ve iOS simülatörde uçtan uca denendi (2026-10-20). iOS'taki iki hata düzeltildi: Ogg Vorbis önizlemesi açılmıyordu, editörde kategori seçilmiyordu.
- [ ] İsteğe bağlı: telefon bağlıyken bana "profil" de. Baseline Profile'ı yeniden üretirim; eskisinde kaldırılan Lottie ekranının satırları var (zararsız, sadece eski).

## Tasarım 2. tur: tema, gezinme, ana sayfa, ayarlar, açılış (yığılı PR'lar, #59'un üstünde)
- [ ] PR'ları sırayla birleştir: theme-shell → home → settings → launch.
- [ ] Telefonda bir kez benchmark çalıştır: zamanlayıcı artık mini oynatıcıdan açılıyor, ses düzeyi de kartın üzerinde kaydırılarak ayarlanıyor. Benchmark adımları buna göre güncellendi ama henüz cihazda koşmadı.

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
- [x] `versionCode` 8, `versionName` 3.0.0 (2026-10-21, etiket `v3.0.0`). Önceki: 7 / 2.0.0.
- [x] API Worker `main` ile aynı sürüme yayınlandı (2026-10-20); oturumsuz istekleri hâlâ reddediyor.
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
- [ ] Internal testing kanalına imzalı AAB yükle. *Release notes* kutusuna `fastlane/metadata/android/en-US/changelogs/8.txt`'yi yapıştır. Play'den kurulan sürümde AI karışımını ve Freesound aramasını dene. Integrity yalnızca Play'den kurulan uygulamada geçer.
- [ ] Sorun yoksa aynı sürümü **Production**'a terfi ettir (*Promote release*). Önce aşağıdaki veri güvenliği formu ve reklam kimliği sorusu tamam olmalı.

## 5. Firebase (Crashlytics ve Analytics)
- [x] Proje `justrelax-app` açıldı (2026-10-18). İki Android uygulaması var: `com.mustafakoceerr.justrelax` ve benchmark için `com.mustafakoceerr.justrelax.benchmark`. `androidApp/google-services.json` yerinde ve git'e girmiyor. Emülatörde denendi: izin yokken hiçbir şey gitmiyor, izin verilince olaylar ve bir çökme raporu gönderiliyor.
- [x] Google Analytics bağlandı; DebugView'de olaylar görüldü (2026-10-18).
- [x] Saklama süresi: olay verisi 2 ay, kullanıcı verisi 14 ay (politikadaki "en fazla 14 ay" ile uyumlu).
- [ ] Yeni bir bilgisayarda ya da CI'da Firebase'li build gerekirse `google-services.json`'u konsoldan indir (*Project settings → Your apps*).
- [x] iOS uygulaması (`com.mustafakoceerr.justrelax`, "JustRelax iOS") aynı projeye eklendi (2026-10-19). `iosApp/iosApp/GoogleService-Info.plist` yerinde ve git'e girmiyor; yeni bir bilgisayarda konsoldan indir. Simülatörde denendi: reddedince ölçüm veritabanı hiç oluşmuyor, izin verilince olaylar gönderiliyor.

## Play mağaza sayfası (görseller hazır)
Yeni ikon ve İngilizce görseller `fastlane/metadata/android/en-US/images/` altında. Nasıl üretildikleri `tools/store-assets/README.md`'de.
- [ ] Play Console → *Grow users → Store presence → Main store listing → Graphics* (İngilizce):
  - **App icon:** `icon.png`
  - **Feature graphic:** `featureGraphic.png`
  - **Phone screenshots:** `phoneScreenshots/` içindeki 8 dosya, 1'den 8'e sırayla.
  - **7-inch tablet screenshots:** `sevenInchScreenshots/` içindeki 8 dosya.
  - **10-inch tablet screenshots:** `tenInchScreenshots/` içindeki 8 dosya.
- [ ] Uygulama adı Play'de nasıl yazılıyor? Görsellerde ve uygulamada "JustRelax"; bildirim ve açılış ekranında "Just Relax". Hangisini istediğini söyle, hepsini ona çeviririm.

## 6. Play Console ve gizlilik
- [ ] Veri güvenliği formunu `docs/play-data-safety.md`'ye göre doldur. AI cümlesi satırında artık **Paylaşılıyor: Evet**.
- [ ] *Uygulama içeriği → Reklam kimliği* sorusuna **Hayır** de. Manifestte artık `AD_ID` izni yok (#56); eski bir beyanda "Evet" varsa düzelt.
- [x] Gizlilik politikası güncellendi ve 12 dilde yayında (2026-10-18). Kaynağı `docs/legal/`.
- [x] Politika OpenAI'ın veri paylaşımına göre güncellendi; kullanım koşulları yeniden yazıldı. İkisi de 12 dilde yayında, kaynakları `docs/legal/` (2026-10-18).

## 7. İçerik
- [ ] 15 yerleşik sesin kaynak lisanslarını doğrula (`tools/sound-pipeline/manifest.json`'daki `license_note`). Şu an `OWN` olarak yayında.
- [ ] Son ses seçimini yap. Pipeline, yükleme ve önbellek temizleme adımları `tools/sound-pipeline/README.md`'de.
- [ ] Kendi seslerimden kaydedilmiş bir döngüde başa dönüşü dinle. AAC'de boşluk ya da tık varsa söyle; yedek çözüm hazır.

## iOS sesi
Simülatörde doğrulananlar (2026-10-19):
- Uygulama arka plandayken ses sürüyor.
- Uyku zamanlayıcısı arka planda süresi dolunca sesi durduruyor.
- 10 dakikalık bir döngü belleği şişirmeden çalıyor.
- Güncellemeden sonra indirilen sesler bulunuyor.
- Kilit ekranına giden bilgi (başlık, alt yazı, görsel, oynat/duraklat/Durdur) test ediliyor.

Simülatör kilit ekranında uygulamaların sesini göstermiyor ve uygulamalar arasında hakemlik yapmıyor (Safari ile JustRelax aynı anda çalabiliyor). Bu yüzden şunlar ilk TestFlight sürümünde bir iPhone'da (iOS 27+) denenmeli:
- [ ] Kilit ekranında ve Denetim Merkezi'nde JustRelax görünüyor: "Rahatlatıcı Sesler" başlığı, yaprak görseli, oynat/duraklat çalışıyor.
- [ ] Başka bir uygulama (Müzik, YouTube) çalmaya başlayınca JustRelax susuyor ve kendiliğinden devam etmiyor.
- [ ] Arama gelince ses duruyor, kapanınca devam ediyor; kulaklığı çıkarınca duruyor.
- [ ] Uygulamayı açık bırakıp kilitle, 10 dakika bekle: ses sürüyor.
- [ ] Döngünün başa dönüşünde boşluk ya da tık var mı (yerleşik bir ses ve içe aktarılmış uzun bir ses)?

## Bahşiş (#48)
Ekran ve mimari hazır; debug sürümünde örnek fiyatlarla görünüyor, yayın sürümünde gizli. Gerçek ödeme için:
- [ ] Play Console → *Monetize → Products → In-app products*: üç tüketilebilir (consumable) ürün aç: `tip_small`, `tip_medium`, `tip_large`. Fiyatları sen seç.
- [ ] Play Console'da ödeme profili ve banka hesabı (Payments profile).
- [ ] App Store Connect'te aynı id'lerle üç *Consumable* ürün (iOS yayınına doğru).
- [ ] Hazır olunca söyle: Play Billing ve StoreKit'i `TipRepository`'nin arkasına bağlarım; satır o zaman yayında da görünür.

## iOS yayını
Xcode projesi hazır (bundle id `com.mustafakoceerr.justrelax`, sürüm 1.3.0). iOS arayüzü SwiftUI ile yeniden yazılıyor (2026-10-19). O bitene kadar uygulama boş bir ekran gösterir ve `PlaySoundUITests` atlanır; bu yüzden TestFlight işleri ekranlar bitince anlamlı.
- [ ] Xcode → *Settings → Accounts*'ta Apple ID'nle giriş yap. Team ID'yi (developer.apple.com → *Membership*) bana söyle ya da `iosApp/Configuration/Config.xcconfig`'teki `TEAM_ID=` satırına yaz. Sunucunun App Attest kontrolü de bunu bekliyor.
- [ ] Team ID belli olunca `server/api-proxy/wrangler.jsonc`'deki `APPLE_APP_ID`'yi `<Team ID>.com.mustafakoceerr.justrelax` yap ve `npm run deploy` ile Worker'ı yayınla (ya da bana söyle, ben yaparım). Boşken iOS'ta AI ve Freesound "doğrulanamadı" der.
- [ ] İlk TestFlight sürümünde AI karışımını ve Freesound aramasını dene. App Attest yalnızca gerçek iPhone'da çalışır; sunucu tarafı gerçek Apple verileriyle test edildi ama cihazda hiç denenmedi.
- [ ] App Store Connect'te *My Apps → +* ile `com.mustafakoceerr.justrelax` bundle id'li uygulamayı aç. App ID'de **App Attest** yeteneği açık olmalı; Xcode otomatik imzalamada kendisi açar.
- [ ] App Store Connect → *App Privacy* anketini `iosApp/iosApp/PrivacyInfo.xcprivacy`'deki listeye göre doldur: kullanıcı içeriği (AI cümlesi), arama geçmişi, cihaz kimliği, ürün etkileşimi ve çökme verisi; hiçbiri kimliğe bağlı değil, takip yok.
- [ ] Crashlytics iOS'ta henüz gerçek bir çökmeyle denenmedi; ilk TestFlight sürümünde Ayarlar'da izin verip konsolda raporların geldiğine bak.

## 8. İsteğe bağlı / sonra
- [x] Main'e birleşmiş eski dallar GitHub'dan ve yerelden silindi (2026-10-19).
- [ ] İstersen GitHub → *Settings → General → Automatically delete head branches*'i aç; birleşen PR dalları kendiliğinden silinir.
