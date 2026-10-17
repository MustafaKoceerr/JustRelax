# Ses pipeline'ı

`pipeline.py` bir manifest'ten v2 ses paketi üretir: `sounds-v2.json` ve `audio/*.ogg` (Opus, 48 kHz, mono).

- **Zemin (BED):** tek bir döngü. Sonu, çapraz geçişle başına bağlanıyor; dikişte boşluk ya da tık olmuyor.
- **Olay (EVENT):** kısa klipler. Elle (`clips`) ya da ses seviyesine göre otomatik (`detect`) kesiliyor.
- **Ses seviyesi:** her dosyaya tek bir kazanç uygulanıyor. Zemin −24 LUFS, olay −20 LUFS, tepe en fazla −1 dBTP. Kazanç sabit olduğu için döngü dikişi bozulmuyor.

Gerekenler: `python3`, `curl`, libopus'lu `ffmpeg`/`ffprobe`.

## Komutlar

```sh
# Testler (saf fonksiyonlar)
python3 -m unittest discover -s tools/sound-pipeline

# Dinleme için taslak paket ("TODO" lisanslarına izin verir) → dist/v2/review.html
python3 tools/sound-pipeline/pipeline.py tools/sound-pipeline/manifest.json tools/sound-pipeline/dist/v2 --draft

# Yayın paketi (bütün lisanslar onaylı olmalı)
python3 tools/sound-pipeline/pipeline.py tools/sound-pipeline/manifest.json tools/sound-pipeline/dist/v2

# Debug build'in APK içindeki paketi
python3 tools/sound-pipeline/pipeline.py tools/sound-pipeline/debug-manifest.json androidApp/src/debug/assets/debug-pack --no-review
```

Debug paketini yeniden ürettikten sonra sözleşme testini çalıştır:
`./gradlew :core:data:testAndroidHostTest --tests '*PipelineCatalogContractTest'`

## Manifest

```json
{"sounds": [
  {"id": "rain_light", "source": "v1:rain_light", "category": "RAIN", "kind": "BED", "license": "CC0",
   "tags": ["rain"], "is_initial": true, "bed": {"start": 5, "length": 40, "crossfade": 3}},
  {"id": "owl", "source": "raw/owl.wav", "names": {"en": "Owl", "tr": "Baykuş"}, "category": "NATURE",
   "icon": "BIRD", "kind": "EVENT", "default_frequency": "RARE", "license": "CC_BY_4_0",
   "attribution": {"title": "Owl at night", "author": "someone", "url": "https://freesound.org/s/1/"},
   "detect": {"count": 3, "min_length": 0.5, "max_length": 6}}
]}
```

- `source`:
  - `v1:<id>` eski CDN'deki mp3'ü indirir. İsimler v1 kataloğundan 12 dilde gelir; `names` alanı bunları ezer.
  - Başka bir değer, manifest'e göre göreli bir dosya yoludur. Bu durumda `names.en` zorunlu.
- **Olay klipleri:** `clips: [{"start", "length"}]` ya da `detect: {"count", "min_length", "max_length"}`.
- **`detect` nasıl çalışır:** taban seviyesinin 10 dB üstüne çıkan bir tepe arıyor ve bölgeyi, ses tabanın 4 dB üstünde kaldığı sürece uzatıyor. Hiç klip çıkmazsa pipeline hata verir; o zaman `clips` ile elle yaz.
- **Geçerli değerler:** `category`, `icon`, `kind`, `default_frequency` ve `license` adları `core:model` enum'larıyla aynı olmalı.
- **Atıf:** CC-BY lisansı `attribution` istiyor.
- **`license_note`:** yalnızca hatırlatma içindir, kataloğa yazılmaz. Şu an 15 sesin hepsi v1'deki gibi `OWN` olarak yayında; gerçek lisanslar doğrulanınca güncellenecek.

## Yayına alma

1. `review.html`'i dinle, `manifest.json`'daki sesleri ve lisansları kesinleştir. `TODO` kalmamalı.
2. `--draft` olmadan çalıştır.
3. `dist/v2/` içeriğini Cloudflare R2'deki `justrelax-assets` bucket'ına, `v2/` altına yükle (Wrangler girişli olmalı):

   ```sh
   cd tools/sound-pipeline/dist/v2
   for f in audio/*.ogg; do
     npx wrangler r2 object put "justrelax-assets/v2/$f" --file "$f" \
       --content-type audio/ogg --cache-control "public, max-age=86400" --remote
   done
   npx wrangler r2 object put justrelax-assets/v2/sounds-v2.json --file sounds-v2.json \
     --content-type "application/json; charset=utf-8" --cache-control "public, max-age=300" --remote
   ```

   Uygulama `<CDN>/v2/sounds-v2.json`'u okur; klip URL'leri bu dosyaya göre görelidir.
4. `cdn.mustafakoceerr.com` önbellekli bir kuralla sunuluyor: klipler 1 gün, katalog 5 dakika önbellekte kalır. Var olan bir dosyanın üzerine yazdıysan Cloudflare panelinden (Caching → Purge Cache → Custom Purge) o URL'leri temizle; temizleme ücretsizdir. Yeni dosya adları için gerekmez.

Maliyet: R2'nin ücretsiz kotası (10 GB depolama, ayda 1 milyon yazma, 10 milyon okuma; dışarıya veri aktarımı her zaman ücretsiz) bu paket için fazlasıyla yeter. Önbellekten sunulan indirmeler R2 okuma kotasına sayılmaz.
