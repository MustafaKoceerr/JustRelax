# Senin yapacakların

Claude'un tek başına yapamadığı işler, öncelik sırasıyla. Biten maddeyi silebilir ya da işaretleyebilirsin.

## 1. PR'ları birleştir
- [x] #36–#48 main'e birleştirildi (2026-10-18, main CI yeşil).

## Performans ölçümü
Telefonda yapıldı; sonuçlar `docs/perf/results.md`'de. Benchmark sürümü ayrı bir paket adıyla (`com.mustafakoceerr.justrelax.benchmark`) kuruluyor ve Play sürümüyle yan yana durabiliyor.
- [ ] Telefonundaki JustRelax'ı Play Store'dan yeniden kur (ilk denemede ana kullanıcıdan kaldırıldı).
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
- [ ] **Yükleme anahtarı:** eski jks'nin şifresi unutuldu. Play Console → *Test et ve yayınla → Uygulama bütünlüğü → Play Uygulama İmzalama → Ayarlar*. "Uygulama imzalama anahtarı sertifikası" ile "Yükleme anahtarı sertifikası" ayrı görünüyorsa:
  1. Android Studio'da yeni anahtar oluştur: *Build → Generate Signed App Bundle → Create new…*, alias `upload`.
  2. `keytool -export -rfc -keystore <jks> -alias upload -file upload_certificate.pem` komutuyla sertifikayı çıkar.
  3. *Yükleme anahtarı sıfırlama isteğinde bulun* ile `.pem`'i gönder.
  Şifreyi şifre yöneticine, jks'yi Drive'a koy. Sayfa farklı görünüyorsa Claude'a göster.
- [ ] `androidApp/build.gradle.kts`'teki `versionCode` (şu an 5), Play'deki son sürümün kodundan büyük olmalı.
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
- [ ] GitHub'daki eski 12 dal (`hotfix`, `improve-ui`, `Multimodules` vb.): istersen silinebilir.
