import { decodeCbor } from "./cbor";
import { parseCertificate, ecdsaSignatureToRaw, readElements, readSingle, expectTag, TAG_OCTET_STRING, TAG_SEQUENCE, type Certificate } from "./der";
import { isRecord } from "./encoding";

/**
 * Apple App Attest, checked as Apple describes in "Validating apps that connect to your server".
 * An attestation proves that a key was made by the genuine app on a genuine device; later
 * assertions are signed by that key.
 */

export type AttestationResult =
  | { ok: true; publicKey: Uint8Array }
  | { ok: false; reason: "malformed" | "chain" | "nonce" | "key_id" | "app_id" | "counter" | "environment" };

export type AssertionResult = { ok: true; counter: number } | { ok: false; reason: "malformed" | "signature" | "app_id" | "counter" };

/** Apple App Attestation Root CA, from https://www.apple.com/certificateauthority/private/ (valid until 2045). */
const APPLE_ROOT_CA: string =
  "MIICITCCAaegAwIBAgIQC/O+DvHN0uD7jG5yH2IXmDAKBggqhkjOPQQDAzBSMSYwJAYDVQQDDB1BcHBsZSBBcHAgQXR0ZXN0YXRpb24gUm9vdCBDQTETMBEGA1UECgwKQXBwbGUgSW5jLjETMBEGA1UECAwKQ2FsaWZvcm5pYTAeFw0yMDAzMTgxODMyNTNaFw00NTAzMTUwMDAwMDBaMFIxJjAkBgNVBAMMHUFwcGxlIEFwcCBBdHRlc3RhdGlvbiBSb290IENBMRMwEQYDVQQKDApBcHBsZSBJbmMuMRMwEQYDVQQIDApDYWxpZm9ybmlhMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAERTHhmLW07ATaFQIEVwTtT4dyctdhNbJhFs/Ii2FdCgAHGbpphY3+d8qjuDngIN3WVhQUBHAoMeQ/cLiP1sOUtgjqK9auYen1mMEvRq9Sk3Jm5X8U62H+xTD3FE9TgS41o0IwQDAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBSskRBTM72+aEH/pwyp5frq5eWKoTAOBgNVHQ8BAf8EBAMCAQYwCgYIKoZIzj0EAwMDaAAwZQIwQgFGnByvsiVbpTKwSga0kP0e8EeDS4+sQmTvb7vn53O5+FRXgeLhpJ06ysC5PrOyAjEAp5U4xDgEgllF7En3VcE3iexZZtKeYnpqtijVoyFraWVIyd/dganmrduC1bmTBGwD";

/** The credential certificate's extension that holds the nonce. */
const NONCE_EXTENSION: string = "1.2.840.113635.100.8.2";

const CURVES: Record<string, { name: string; size: number }> = {
  "1.2.840.10045.3.1.7": { name: "P-256", size: 32 },
  "1.3.132.0.34": { name: "P-384", size: 48 },
};

const SIGNATURE_HASHES: Record<string, string> = {
  "1.2.840.10045.4.3.2": "SHA-256",
  "1.2.840.10045.4.3.3": "SHA-384",
};

/** `appattestdevelop` for builds run from Xcode, `appattest` and seven zero bytes for TestFlight and the App Store. */
const AAGUIDS: string[] = ["appattestdevelop", "appattest\0\0\0\0\0\0\0"];

/**
 * Checks an attestation object for [clientData] and the key [keyId] (base64, as the app gets it
 * from `DCAppAttestService.generateKey`). [appId] is `<Team ID>.<bundle id>`.
 */
export async function verifyAttestation(
  attestation: Uint8Array,
  clientData: Uint8Array,
  keyId: string,
  appId: string,
  nowMs: number,
): Promise<AttestationResult> {
  const decoded: unknown = decodeCbor(attestation);
  if (!isRecord(decoded) || decoded.fmt !== "apple-appattest" || !isRecord(decoded.attStmt)) return { ok: false, reason: "malformed" };
  const chain: unknown = decoded.attStmt.x5c;
  const authData: unknown = decoded.authData;
  if (!Array.isArray(chain) || chain.length !== 2 || !(authData instanceof Uint8Array) || authData.length < 55) {
    return { ok: false, reason: "malformed" };
  }
  const [leafDer, intermediateDer] = chain as unknown[];
  if (!(leafDer instanceof Uint8Array) || !(intermediateDer instanceof Uint8Array)) return { ok: false, reason: "malformed" };

  let leaf: Certificate;
  let intermediate: Certificate;
  let root: Certificate;
  try {
    leaf = parseCertificate(leafDer);
    intermediate = parseCertificate(intermediateDer);
    root = parseCertificate(base64ToBytes(APPLE_ROOT_CA));
  } catch {
    return { ok: false, reason: "malformed" };
  }

  // 1. The chain leads to Apple's root, and every certificate is valid now.
  const chained: boolean =
    [leaf, intermediate, root].every((certificate: Certificate): boolean => certificate.notBefore <= nowMs && nowMs <= certificate.notAfter) &&
    (await isSignedBy(intermediate, root)) &&
    (await isSignedBy(leaf, intermediate));
  if (!chained) return { ok: false, reason: "chain" };

  // 2-4. The nonce in the credential certificate is sha256(authData || sha256(clientData)).
  const nonce: Uint8Array = await sha256(concat(authData, await sha256(clientData)));
  const embedded: Uint8Array | null = embeddedNonce(leaf);
  if (embedded === null || !equal(embedded, nonce)) return { ok: false, reason: "nonce" };

  // 5. The key id is the hash of the credential's public key.
  if (bytesToBase64(await sha256(leaf.publicKey)) !== keyId) return { ok: false, reason: "key_id" };

  // 6. The RP ID is the hash of our App ID.
  if (!equal(authData.subarray(0, 32), await sha256(new TextEncoder().encode(appId)))) return { ok: false, reason: "app_id" };

  // 7. A new key has never signed anything.
  if (readCounter(authData) !== 0) return { ok: false, reason: "counter" };

  // 8. The key comes from App Attest's development or production environment.
  const aaguid: string = new TextDecoder().decode(authData.subarray(37, 53));
  if (!AAGUIDS.includes(aaguid)) return { ok: false, reason: "environment" };

  // 9. The credential id is the key id.
  const idLength: number = (authData[53]! << 8) | authData[54]!;
  if (bytesToBase64(authData.subarray(55, 55 + idLength)) !== keyId) return { ok: false, reason: "key_id" };

  return { ok: true, publicKey: leaf.publicKey };
}

/**
 * Checks an assertion over [clientData] with the key an attestation returned. The counter only has
 * to be above zero: the proxy keeps no state, so freshness comes from the timestamp in the client
 * data instead of a stored counter.
 */
export async function verifyAssertion(
  assertion: Uint8Array,
  clientData: Uint8Array,
  publicKey: Uint8Array,
  appId: string,
): Promise<AssertionResult> {
  const decoded: unknown = decodeCbor(assertion);
  if (!isRecord(decoded)) return { ok: false, reason: "malformed" };
  const { signature, authenticatorData } = decoded;
  if (!(signature instanceof Uint8Array) || !(authenticatorData instanceof Uint8Array) || authenticatorData.length < 37) {
    return { ok: false, reason: "malformed" };
  }

  const nonce: Uint8Array = await sha256(concat(authenticatorData, await sha256(clientData)));
  let valid: boolean;
  try {
    const key: CryptoKey = await crypto.subtle.importKey("raw", publicKey, { name: "ECDSA", namedCurve: "P-256" }, false, ["verify"]);
    valid = await crypto.subtle.verify({ name: "ECDSA", hash: "SHA-256" }, key, ecdsaSignatureToRaw(signature, 32), nonce);
  } catch {
    valid = false;
  }
  if (!valid) return { ok: false, reason: "signature" };

  if (!equal(authenticatorData.subarray(0, 32), await sha256(new TextEncoder().encode(appId)))) return { ok: false, reason: "app_id" };
  const counter: number = readCounter(authenticatorData);
  if (counter <= 0) return { ok: false, reason: "counter" };
  return { ok: true, counter };
}

async function isSignedBy(certificate: Certificate, issuer: Certificate): Promise<boolean> {
  const curve: { name: string; size: number } | undefined = CURVES[issuer.curve];
  const hash: string | undefined = SIGNATURE_HASHES[certificate.signatureAlgorithm];
  if (curve === undefined || hash === undefined) return false;
  try {
    const key: CryptoKey = await crypto.subtle.importKey("spki", issuer.spki, { name: "ECDSA", namedCurve: curve.name }, false, ["verify"]);
    return await crypto.subtle.verify({ name: "ECDSA", hash }, key, ecdsaSignatureToRaw(certificate.signature, curve.size), certificate.signed);
  } catch {
    return false;
  }
}

/** The nonce extension is `SEQUENCE { [1] EXPLICIT OCTET STRING nonce }`. */
function embeddedNonce(certificate: Certificate): Uint8Array | null {
  const value: Uint8Array | undefined = certificate.extensions.get(NONCE_EXTENSION);
  if (value === undefined) return null;
  try {
    const [tagged] = readElements(readSingle(value, TAG_SEQUENCE).value);
    return expectTag(readSingle(tagged!.value, TAG_OCTET_STRING), TAG_OCTET_STRING).value;
  } catch {
    return null;
  }
}

/** The big-endian sign counter after the RP ID hash and the flags byte. */
function readCounter(authData: Uint8Array): number {
  return ((authData[33]! << 24) >>> 0) + ((authData[34]! << 16) | (authData[35]! << 8) | authData[36]!);
}

async function sha256(bytes: Uint8Array): Promise<Uint8Array> {
  return new Uint8Array(await crypto.subtle.digest("SHA-256", bytes));
}

function concat(a: Uint8Array, b: Uint8Array): Uint8Array {
  const out: Uint8Array = new Uint8Array(a.length + b.length);
  out.set(a);
  out.set(b, a.length);
  return out;
}

function equal(a: Uint8Array, b: Uint8Array): boolean {
  return a.length === b.length && a.every((byte: number, i: number): boolean => byte === b[i]);
}

export function base64ToBytes(text: string): Uint8Array {
  return Uint8Array.from(atob(text), (char: string): number => char.charCodeAt(0));
}

export function bytesToBase64(bytes: Uint8Array): string {
  let binary: string = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary);
}
