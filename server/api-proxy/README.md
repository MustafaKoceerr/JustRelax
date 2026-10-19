# JustRelax API proxy

OpenAI ve Freesound anahtarlarını uygulamanın dışında tutan Cloudflare Worker. Ücretsiz Workers planında çalışır ve `https://api.mustafakoceerr.com/` adresinde yayındadır.

| Uç nokta | Ne yapar | İstek sınırı |
|---|---|---|
| `POST /v1/session` | Android: Play Integrity belirtecini Google'a doğrulatır. iOS: App Attest kanıtını (`appattest.v1.…`) kendisi doğrular. İkisinde de 1 saatlik oturum belirteci verir | IP başına 5/dk |
| `POST /v1/ai/mix` | OpenAI'a sunucudaki model ve talimatla sorar | Kurulum başına 5/dk, IP başına 15/dk, toplam 120/dk |
| `GET /v1/freesound/search` | Lisans/süre filtresini ekler, anahtarı başlıkta gönderir | Kurulum başına 30/dk |

## Secret'lar

```sh
npx wrangler secret put OPENAI_API_KEY
npx wrangler secret put FREESOUND_API_KEY
npx wrangler secret put SESSION_SECRET          # uzun, rastgele bir değer
npx wrangler secret put GOOGLE_SERVICE_ACCOUNT_JSON < servis-hesabi.json
```

Servis hesabı, Play Console'da *App integrity → Play Integrity API* altında uygulamaya bağlanan Google Cloud projesinde oluşturulur ve rol gerektirmez.

## iOS: App Attest

`wrangler.jsonc`'deki `APPLE_APP_ID` değişkeni `<Team ID>.com.mustafakoceerr.justrelax` olmalı; boşken iOS oturumları `503 appattest_unavailable` alır. Akış durumsuzdur, sunucu hiçbir şey saklamaz:

1. **Yeni anahtar:** Uygulama anahtar üretir ve `<sessionRequestHash>:<zaman>` için attestation ister. Worker zinciri gömülü Apple kök sertifikasına kadar doğrular, nonce, anahtar kimliği, App ID ve sayaç kontrollerini yapar. Oturumla birlikte, anahtarın açık anahtarını taşıyan imzalı bir `keyToken` döner.
2. **Sonraki oturumlar:** Uygulama aynı veri için assertion imzalar ve `keyToken`'ı gönderir. Worker imzayı bu anahtarla doğrular; Apple'a gidilmez.

Zaman damgası en fazla 10 dakika eski olabilir. Testler gerçek Apple nesneleriyle koşar (`test/appattest-vectors.ts`).

## Komutlar

```sh
npm test            # vitest
npm run typecheck
npm run dev         # yerel sunucu: .dev.vars.example → .dev.vars
npm run deploy
```

Debug uygulama yerel sunucuya bağlanır. Android emülatör ya da cihaz için önce `adb reverse tcp:8787 tcp:8787` çalıştır; iOS simülatörü Mac'in `localhost`'una doğrudan ulaşır. Yerelde `INTEGRITY_REQUIRED=false`; yayındaki Worker'da Integrity kontrolü her zaman açıktır.
