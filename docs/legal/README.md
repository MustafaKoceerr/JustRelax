# Legal pages

The source of the pages the app links to from Settings: `cdn.mustafakoceerr.com/legal/<language>/privacy-policy.html` (`RemoteEndpoints`). There is one file per app language, and all of them say the same thing. When one changes, change all 12 and update the effective date.

`terms-conditions.html` is still only on the CDN.

Publish (from `server/api-proxy`, where wrangler is logged in):
```
for l in en tr de es fr hi it ja ko nl pt sv; do
  npx wrangler r2 object put justrelax-assets/legal/$l/privacy-policy.html \
    --file ../../docs/legal/$l/privacy-policy.html \
    --content-type "text/html; charset=utf-8" --cache-control "public, max-age=3600" --remote
done
```
Then purge the 12 URLs from the Cloudflare cache (zone `mustafakoceerr.com`); otherwise the old page stays up to an hour.

The policy has to match what the app sends; `docs/play-data-safety.md` lists the data flows.
