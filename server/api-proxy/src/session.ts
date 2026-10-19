import { base64UrlDecode, base64UrlEncode, isRecord } from "./encoding";

/** How long a session lasts after a successful integrity check. */
export const SESSION_TTL_MS: number = 60 * 60 * 1_000;

/**
 * A session token is `payload.signature`: the install id and expiry, signed with HMAC-SHA256. The
 * app gets one after a Play Integrity or App Attest check and sends it with every API call, so the
 * check runs about once an hour per install instead of on every request.
 */
export async function issueSession(installId: string, nowMs: number, secret: string): Promise<string> {
  return sign({ sub: installId, exp: nowMs + SESSION_TTL_MS }, secret);
}

/** The install id of a valid, unexpired token; null otherwise. */
export async function verifySession(token: string, nowMs: number, secret: string): Promise<string | null> {
  const claims: Record<string, unknown> | null = await verify(token, secret);
  if (claims === null || typeof claims.sub !== "string" || typeof claims.exp !== "number" || "typ" in claims) return null;
  return claims.exp >= nowMs ? claims.sub : null;
}

/** An App Attest key the proxy has checked once, for the assertions that follow. */
export interface AttestedKey {
  installId: string;
  keyId: string;
  /** The uncompressed P-256 point. */
  publicKey: Uint8Array;
}

const KEY_TOKEN_TYPE: string = "appattest-key";

/**
 * The app keeps this after its key's attestation and sends it with each later assertion. It is
 * signed like a session but has no expiry, as the key lives as long as the install; the proxy
 * stores nothing.
 */
export async function issueKeyToken(key: AttestedKey, secret: string): Promise<string> {
  return sign({ typ: KEY_TOKEN_TYPE, sub: key.installId, kid: key.keyId, pk: base64UrlEncode(key.publicKey) }, secret);
}

export async function verifyKeyToken(token: string, secret: string): Promise<AttestedKey | null> {
  const claims: Record<string, unknown> | null = await verify(token, secret);
  if (claims === null || claims.typ !== KEY_TOKEN_TYPE) return null;
  const { sub, kid, pk } = claims;
  if (typeof sub !== "string" || typeof kid !== "string" || typeof pk !== "string") return null;
  const publicKey: Uint8Array | null = base64UrlDecode(pk);
  return publicKey === null ? null : { installId: sub, keyId: kid, publicKey };
}

async function sign(claims: Record<string, unknown>, secret: string): Promise<string> {
  const payload: string = base64UrlEncode(new TextEncoder().encode(JSON.stringify(claims)));
  const signature: ArrayBuffer = await crypto.subtle.sign("HMAC", await hmacKey(secret), new TextEncoder().encode(payload));
  return `${payload}.${base64UrlEncode(new Uint8Array(signature))}`;
}

async function verify(token: string, secret: string): Promise<Record<string, unknown> | null> {
  const parts: string[] = token.split(".");
  if (parts.length !== 2) return null;
  const [payload, signatureText] = parts as [string, string];
  const signature: Uint8Array | null = base64UrlDecode(signatureText);
  const payloadBytes: Uint8Array | null = base64UrlDecode(payload);
  if (signature === null || signature.length === 0 || payloadBytes === null) return null;

  const valid: boolean = await crypto.subtle.verify("HMAC", await hmacKey(secret), signature, new TextEncoder().encode(payload));
  if (!valid) return null;

  try {
    const claims: unknown = JSON.parse(new TextDecoder().decode(payloadBytes));
    return isRecord(claims) ? claims : null;
  } catch {
    return null;
  }
}

async function hmacKey(secret: string): Promise<CryptoKey> {
  return crypto.subtle.importKey("raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign", "verify"]);
}
