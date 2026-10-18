# Play Console: Veri güvenliği formu

JustRelax v2'nin dışarıya ne gönderdiğine göre hazırlandı (2026-10-18). Reklam yok. Firebase Analytics ve Crashlytics yalnızca kullanıcı ilk açılışta (ya da Ayarlar'da) izin verirse çalışır; izin yoksa hiçbir şey gönderilmez.

Google'ın tanımı: cihazdan **dışarı gönderilen** veri "toplanmış" sayılır. Yalnızca cihazda işlenen veri beyan edilmez.

## Dışarı giden veriler

| Ne | Nereye | Ne zaman |
|---|---|---|
| AI karışımı için yazılan cümle ve indirilmiş seslerin adları/etiketleri | Kendi proxy'miz (`api.mustafakoceerr.com`) üzerinden OpenAI | Kullanıcı AI karışımı istediğinde |
| Freesound arama metni | Proxy üzerinden Freesound | Kullanıcı arama yaptığında |
| Kurulum kimliği (rastgele, uygulamaya özel, kişiyle bağlı değil) | Proxy | AI/arama kullanılırken, istek sınırı için |
| Play Integrity belirteci | Proxy üzerinden Google | Oturum açılırken, saatte en fazla bir kez |
| Ses indirme istekleri | CDN (`cdn.mustafakoceerr.com`) | Ses indirilirken; içerik yok, yalnızca dosya adı |
| Kullanım olayları (ekran adı, çalınan katalog sesinin kimliği ve kategorisi, kaydedilen karışımın ses sayısı, AI isteği ve hata türü, arama sonuç sayısı, içe aktarılan sesin türü, zamanlayıcı süresi aralığı) ve Firebase uygulama örneği kimliği | Google Firebase Analytics | Yalnızca izin verildiyse |
| Çökme raporları ve yakalanan hatalar (yığın izi, cihaz modeli, Android sürümü) | Google Firebase Crashlytics | Yalnızca izin verildiyse |

Proxy hiçbir şeyi saklamaz. İstek sınırları yalnızca 60 saniyelik sayaçlardır. Cloudflare Workers günlükleri istek yolunu (arama metni dahil) en fazla 3 gün tutar.

## Cihazdan hiç çıkmayanlar (beyan edilmez)

- **İçe aktarılan ses dosyaları:** `files/audio/user` altında kalır, hiçbir sunucuya gönderilmez. Android yedeğine de girmez; yedekleme yalnızca ayarlar dosyasını alır (`data_extraction_rules.xml`).
- **Kayıtlı karışımlar ve kütüphane:** cihazdaki veritabanında kalır.

## Formdaki cevaplar

**Veri toplama ve güvenlik**
- Uygulama gerekli kullanıcı veri türlerinden herhangi birini topluyor veya paylaşıyor mu? → **Evet**
- Tüm kullanıcı verileri aktarım sırasında şifreleniyor mu? → **Evet** (yalnızca HTTPS; release build düz HTTP'ye izin vermez)
- Kullanıcıların verilerinin silinmesini istemesi için bir yol sunuyor musunuz? → **Hayır**. Hesap yok ve sunucuda kullanıcı verisi saklanmıyor. İstersen "Evet" deyip gizlilik politikasında e-posta adresi verebilirsin.

**Veri türleri**

| Kategori → Tür | Toplanıyor | Paylaşılıyor | Geçici işleniyor | Zorunlu mu | Amaç |
|---|---|---|---|---|---|
| Uygulama etkinliği → Diğer kullanıcı tarafından oluşturulan içerik (AI cümlesi) | Evet | Hayır¹ | Hayır² | İsteğe bağlı | Uygulama işlevi |
| Uygulama etkinliği → Uygulama içi arama geçmişi (Freesound araması) | Evet | Hayır³ | Hayır² | İsteğe bağlı | Uygulama işlevi |
| Cihaz veya diğer kimlikler → Cihaz veya diğer kimlikler (kurulum kimliği, Play Integrity, Firebase uygulama örneği kimliği) | Evet | Hayır | Hayır | İsteğe bağlı⁴ | Dolandırıcılığı önleme, güvenlik ve uyumluluk; Analiz |
| Uygulama etkinliği → Uygulama etkileşimleri (Analytics olayları) | Evet | Hayır⁵ | Hayır | İsteğe bağlı (onay) | Analiz |
| Uygulama bilgileri ve performans → Kilitlenme günlükleri | Evet | Hayır⁵ | Hayır | İsteğe bağlı (onay) | Analiz, Uygulama işlevi |
| Uygulama bilgileri ve performans → Diğer uygulama performans verileri / Teşhis | Evet | Hayır⁵ | Hayır | İsteğe bağlı (onay) | Analiz |

1. OpenAI, isteği bizim adımıza işleyen bir hizmet sağlayıcıdır. Google'ın tanımında bu "paylaşım" sayılmaz.
2. Proxy saklamıyor, ama OpenAI kötüye kullanım denetimi için API isteklerini bir süre tutabiliyor ve Workers günlükleri 3 gün kalıyor. Bu yüzden ihtiyatlı cevap "Hayır".
3. Arama, kullanıcının başlattığı ve verinin Freesound'a gideceğini beklediği bir işlem. Google bunu "paylaşım" saymaz.
4. Kurulum kimliği yalnızca AI ve Freesound kullanılınca; Firebase kimliği yalnızca Analytics onayı varsa.
5. Google Firebase bizim adımıza işleyen bir hizmet sağlayıcıdır. Google'ın tanımında bu "paylaşım" sayılmaz.

**Beyan edilmeyenler:** Ses kayıtları, fotoğraf/video, dosyalar, konum, kişisel bilgiler, finansal bilgiler, sağlık, mesajlar, kişiler, takvim. Reklam kimliği (AD_ID) toplanmıyor.

## Bunlar da gerekiyor

- **Gizlilik politikası** (`cdn.mustafakoceerr.com/legal/<dil>/privacy-policy.html`) şu üç noktayı söylemeli:
  - AI cümlesinin OpenAI'a, aramaların Freesound'a gittiği.
  - Rastgele kurulum kimliğinin yalnızca kötüye kullanımı önlemek için kullanıldığı.
  - Firebase Analytics ve Crashlytics'in yalnızca izinle çalıştığı, neyi topladığı ve iznin Ayarlar'dan geri alınabileceği.
  - Kullanıcının içe aktardığı seslerin cihazdan çıkmadığı.
- **Mikrofon:** Uygulama mikrofon kullanmıyor; `RECORD_AUDIO` izni yok.
- **Play Integrity:** Formdaki "Cihaz veya diğer kimlikler" satırı bunu kapsıyor. Yayından önce Google'ın güncel rehberini bir kez kontrol et: <https://developer.android.com/google/play/integrity/overview>
