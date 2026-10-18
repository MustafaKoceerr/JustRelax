import { base64UrlEncode, isRecord } from "./encoding";

export interface ServiceAccount {
  clientEmail: string;
  privateKeyPem: string;
}

type Fetch = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;

const TOKEN_URL: string = "https://oauth2.googleapis.com/token";
const SCOPE: string = "https://www.googleapis.com/auth/playintegrity";

/** Reads the JSON key of the Google Cloud service account linked to the app in Play Console. */
export function parseServiceAccount(json: string): ServiceAccount | null {
  let parsed: unknown;
  try {
    parsed = JSON.parse(json);
  } catch {
    return null;
  }
  if (!isRecord(parsed) || typeof parsed.client_email !== "string" || typeof parsed.private_key !== "string") return null;
  return { clientEmail: parsed.client_email, privateKeyPem: parsed.private_key };
}

/**
 * Decodes Play Integrity tokens on Google's servers (the recommended way). It signs a service
 * account assertion, exchanges it for an access token and keeps that token until shortly before it
 * expires, so most requests make a single call to Google.
 */
export class GoogleIntegrityDecoder {
  private accessToken: string | null = null;
  private accessTokenExpiresAtMs: number = 0;

  constructor(
    private readonly account: ServiceAccount,
    private readonly packageName: string,
    private readonly fetch: Fetch,
    private readonly now: () => number,
  ) {}

  /** The verdict (`tokenPayloadExternal`), or null if Google could not decode the token. */
  async decode(integrityToken: string): Promise<unknown> {
    const accessToken: string | null = await this.getAccessToken();
    if (accessToken === null) return null;
    const response: Response = await this.fetch(
      `https://playintegrity.googleapis.com/v1/${this.packageName}:decodeIntegrityToken`,
      {
        method: "POST",
        headers: { authorization: `Bearer ${accessToken}`, "content-type": "application/json" },
        body: JSON.stringify({ integrity_token: integrityToken }),
      },
    );
    if (!response.ok) return null;
    const body: unknown = await response.json();
    return isRecord(body) ? (body.tokenPayloadExternal ?? null) : null;
  }

  private async getAccessToken(): Promise<string | null> {
    const nowMs: number = this.now();
    if (this.accessToken !== null && nowMs < this.accessTokenExpiresAtMs) return this.accessToken;

    const response: Response = await this.fetch(TOKEN_URL, {
      method: "POST",
      headers: { "content-type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
        assertion: await this.signAssertion(Math.floor(nowMs / 1_000)),
      }).toString(),
    });
    if (!response.ok) return null;
    const body: unknown = await response.json();
    if (!isRecord(body) || typeof body.access_token !== "string" || typeof body.expires_in !== "number") return null;
    this.accessToken = body.access_token;
    this.accessTokenExpiresAtMs = nowMs + (body.expires_in - 60) * 1_000;
    return this.accessToken;
  }

  private async signAssertion(nowSeconds: number): Promise<string> {
    const encode = (value: object): string => base64UrlEncode(new TextEncoder().encode(JSON.stringify(value)));
    const unsigned: string = `${encode({ alg: "RS256", typ: "JWT" })}.${encode({
      iss: this.account.clientEmail,
      scope: SCOPE,
      aud: TOKEN_URL,
      iat: nowSeconds,
      exp: nowSeconds + 3_600,
    })}`;
    const key: CryptoKey = await crypto.subtle.importKey(
      "pkcs8",
      pemToDer(this.account.privateKeyPem),
      { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
      false,
      ["sign"],
    );
    const signature: ArrayBuffer = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(unsigned));
    return `${unsigned}.${base64UrlEncode(new Uint8Array(signature))}`;
  }
}

function pemToDer(pem: string): Uint8Array {
  const body: string = pem.replace(/-----(BEGIN|END) PRIVATE KEY-----/g, "").replace(/\s+/g, "");
  return Uint8Array.from(atob(body), (char: string): number => char.charCodeAt(0));
}
