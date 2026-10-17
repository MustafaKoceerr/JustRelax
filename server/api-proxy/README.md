# JustRelax API proxy

OpenAI ve Freesound anahtarlarını uygulamanın dışında tutan Cloudflare Worker. Ücretsiz Workers planında çalışır ve `https://api.mustafakoceerr.com/` adresinde yayındadır.

| Uç nokta | Ne yapar | İstek sınırı |
|---|---|---|
| `POST /v1/session` | Play Integrity belirtecini Google'a doğrulatır, 1 saatlik oturum belirteci verir | IP başına 5/dk |
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

## Komutlar

```sh
npm test            # vitest
npm run typecheck
npm run dev         # yerel sunucu: .dev.vars.example → .dev.vars
npm run deploy
```

Debug uygulama yerel sunucuya bağlanır. Emülatör ya da cihaz için önce `adb reverse tcp:8787 tcp:8787` çalıştır. Yerelde `INTEGRITY_REQUIRED=false`; yayındaki Worker'da Integrity kontrolü her zaman açıktır.
