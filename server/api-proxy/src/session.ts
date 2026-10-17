import { base64UrlDecode, base64UrlEncode, isRecord } from "./encoding";

/** How long a session lasts after a successful integrity check. */
export const SESSION_TTL_MS: number = 60 * 60 * 1_000;

/**
 * A session token is `payload.signature`: the install id and expiry, signed with HMAC-SHA256. The
 * app gets one after a Play Integrity check and sends it with every API call, so the check runs
 * about once an hour per install instead of on every request.
 */
export async function issueSession(installId: string, nowMs: number, secret: string): Promise<string> {
  const payload: string = base64UrlEncode(new TextEncoder().encode(JSON.stringify({ sub: installId, exp: nowMs + SESSION_TTL_MS })));
  const signature: ArrayBuffer = await crypto.subtle.sign("HMAC", await hmacKey(secret), new TextEncoder().encode(payload));
  return `${payload}.${base64UrlEncode(new Uint8Array(signature))}`;
}

/** The install id of a valid, unexpired token; null otherwise. */
export async function verifySession(token: string, nowMs: number, secret: string): Promise<string | null> {
  const parts: string[] = token.split(".");
  if (parts.length !== 2) return null;
  const [payload, signatureText] = parts as [string, string];
  const signature: Uint8Array | null = base64UrlDecode(signatureText);
  const payloadBytes: Uint8Array | null = base64UrlDecode(payload);
  if (signature === null || signature.length === 0 || payloadBytes === null) return null;

  const valid: boolean = await crypto.subtle.verify("HMAC", await hmacKey(secret), signature, new TextEncoder().encode(payload));
  if (!valid) return null;

  let claims: unknown;
  try {
    claims = JSON.parse(new TextDecoder().decode(payloadBytes));
  } catch {
    return null;
  }
  if (!isRecord(claims) || typeof claims.sub !== "string" || typeof claims.exp !== "number") return null;
  return claims.exp >= nowMs ? claims.sub : null;
}

async function hmacKey(secret: string): Promise<CryptoKey> {
  return crypto.subtle.importKey("raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign", "verify"]);
}
