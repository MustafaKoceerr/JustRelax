import { describe, expect, it } from "vitest";
import { verifyAssertion, verifyAttestation, type AssertionResult, type AttestationResult } from "../src/appattest";
import { decodeCbor } from "../src/cbor";
import { encodeCbor } from "./cbor-encode";
import { APP_ID, ASSERTION, DEVELOPMENT, PRODUCTION, VALID_AT } from "./appattest-vectors";

function base64(text: string): Uint8Array {
  return Uint8Array.from(atob(text), (char: string): number => char.charCodeAt(0));
}

function utf8(text: string): Uint8Array {
  return new TextEncoder().encode(text);
}

async function attest(
  vector: typeof DEVELOPMENT,
  overrides: { appId?: string; clientData?: string; keyId?: string; now?: number; attestation?: Uint8Array } = {},
): Promise<AttestationResult> {
  return verifyAttestation(
    overrides.attestation ?? base64(vector.attestation),
    utf8(overrides.clientData ?? vector.clientData),
    overrides.keyId ?? vector.keyId,
    overrides.appId ?? APP_ID,
    overrides.now ?? VALID_AT,
  );
}

/** The attestation object with one of its two certificates changed by a single byte. */
function withTamperedCertificate(vector: typeof DEVELOPMENT, index: number): Uint8Array {
  const attestation: { attStmt: { x5c: Uint8Array[] } } = decodeCbor(base64(vector.attestation)) as never;
  const certificate: Uint8Array = attestation.attStmt.x5c[index]!;
  // A byte inside the signed part (the subject's common name), so the signature no longer matches.
  certificate[200] = certificate[200]! ^ 0x01;
  return encodeCbor(attestation);
}

describe("verifyAttestation", (): void => {
  it("accepts Apple's development and production attestations and returns the key", async (): Promise<void> => {
    const development: AttestationResult = await attest(DEVELOPMENT);
    const production: AttestationResult = await attest(PRODUCTION);

    expect(development.ok).toBe(true);
    expect(production.ok).toBe(true);
    if (development.ok) expect(development.publicKey.length).toBe(65);
  });

  it("refuses another app's id", async (): Promise<void> => {
    expect(await attest(DEVELOPMENT, { appId: "ABCDE12345.com.mustafakoceerr.justrelax" })).toEqual({ ok: false, reason: "app_id" });
  });

  it("refuses an attestation made for other client data", async (): Promise<void> => {
    expect(await attest(DEVELOPMENT, { clientData: "replayed" })).toEqual({ ok: false, reason: "nonce" });
  });

  it("refuses a key id that is not the certificate's key", async (): Promise<void> => {
    expect(await attest(DEVELOPMENT, { keyId: PRODUCTION.keyId })).toEqual({ ok: false, reason: "key_id" });
  });

  it("refuses certificates that are not signed by Apple's chain", async (): Promise<void> => {
    expect(await attest(DEVELOPMENT, { attestation: withTamperedCertificate(DEVELOPMENT, 0) })).toEqual({ ok: false, reason: "chain" });
    expect(await attest(DEVELOPMENT, { attestation: withTamperedCertificate(DEVELOPMENT, 1) })).toEqual({ ok: false, reason: "chain" });
  });

  it("refuses a leaf certificate outside its validity", async (): Promise<void> => {
    expect(await attest(DEVELOPMENT, { now: Date.UTC(2026, 0, 1) })).toEqual({ ok: false, reason: "chain" });
  });

  it("refuses something that is not an attestation", async (): Promise<void> => {
    expect(await attest(DEVELOPMENT, { attestation: utf8("not cbor at all") })).toEqual({ ok: false, reason: "malformed" });
  });
});

describe("verifyAssertion", (): void => {
  async function assert(overrides: { appId?: string; clientData?: string; publicKey?: Uint8Array } = {}): Promise<AssertionResult> {
    return verifyAssertion(
      base64(ASSERTION.assertion),
      utf8(overrides.clientData ?? ASSERTION.clientData),
      overrides.publicKey ?? base64(ASSERTION.publicKey),
      overrides.appId ?? APP_ID,
    );
  }

  it("accepts an assertion signed by the attested key", async (): Promise<void> => {
    expect(await assert()).toEqual({ ok: true, counter: 1 });
  });

  it("refuses an assertion over other client data", async (): Promise<void> => {
    expect(await assert({ clientData: "something else" })).toEqual({ ok: false, reason: "signature" });
  });

  it("refuses an assertion from another key", async (): Promise<void> => {
    const other: AttestationResult = await attest(DEVELOPMENT);
    if (!other.ok) throw new Error("vector");
    expect(await assert({ publicKey: other.publicKey })).toEqual({ ok: false, reason: "signature" });
  });

  it("refuses another app's id", async (): Promise<void> => {
    expect(await assert({ appId: "ABCDE12345.com.mustafakoceerr.justrelax" })).toEqual({ ok: false, reason: "app_id" });
  });
});
