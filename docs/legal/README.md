# Legal pages

The source of the pages the app links to from Settings: `cdn.mustafakoceerr.com/legal/<language>/privacy-policy.html` and `terms-conditions.html` (`RemoteEndpoints`). There is one file per app language, and all of them say the same thing. When one changes, change all 12 and update the effective date.

The AI screen's first-use notice (`feature/ai`, `AiDataNoticeDialog`) has to match what the policy says about OpenAI.

Publish (from `server/api-proxy`, where wrangler is logged in):
```
for l in en tr de es fr hi it ja ko nl pt sv; do for page in privacy-policy terms-conditions; do
  npx wrangler r2 object put justrelax-assets/legal/$l/$page.html \
    --file ../../docs/legal/$l/$page.html \
    --content-type "text/html; charset=utf-8" --cache-control "public, max-age=3600" --remote
done; done
```
Then purge the 24 URLs from the Cloudflare cache (zone `mustafakoceerr.com`); otherwise the old page stays up to an hour.

The policy has to match what the app sends; `docs/play-data-safety.md` lists the data flows.
