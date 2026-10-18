import { describe, expect, it } from "vitest";
import { base64UrlDecode } from "../src/encoding";
import { GoogleIntegrityDecoder, parseServiceAccount, type ServiceAccount } from "../src/google";

async function serviceAccount(): Promise<{ account: ServiceAccount; publicKey: CryptoKey }> {
  const pair: CryptoKeyPair = (await crypto.subtle.generateKey(
    { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" },
    true,
    ["sign", "verify"],
  )) as CryptoKeyPair;
  const pkcs8: ArrayBuffer = (await crypto.subtle.exportKey("pkcs8", pair.privateKey)) as ArrayBuffer;
  const body: string = btoa(String.fromCharCode(...new Uint8Array(pkcs8))).replace(/(.{64})/g, "$1\n");
  const json: string = JSON.stringify({
    type: "service_account",
    client_email: "integrity@project.iam.gserviceaccount.com",
    private_key: `-----BEGIN PRIVATE KEY-----\n${body}\n-----END PRIVATE KEY-----\n`,
  });
  const account: ServiceAccount | null = parseServiceAccount(json);
  if (account === null) throw new Error("service account did not parse");
  return { account, publicKey: pair.publicKey };
}

interface Call {
  url: string;
  init: RequestInit | undefined;
}

describe("GoogleIntegrityDecoder", () => {
  it("signs an assertion, caches the access token and decodes tokens with it", async () => {
    const { account, publicKey } = await serviceAccount();
    const calls: Call[] = [];
    const fakeFetch = async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      const url: string = String(input);
      calls.push({ url, init });
      if (url === "https://oauth2.googleapis.com/token") {
        return Response.json({ access_token: "access-1", expires_in: 3600 });
      }
      return Response.json({ tokenPayloadExternal: { requestDetails: { requestHash: "h" } } });
    };
    const decoder = new GoogleIntegrityDecoder(account, "com.mustafakoceerr.justrelax", fakeFetch, (): number => 1_800_000_000_000);

    const first: unknown = await decoder.decode("token-a");
    await decoder.decode("token-b");

    expect(first).toEqual({ requestDetails: { requestHash: "h" } });
    expect(calls.map((call: Call): string => call.url)).toEqual([
      "https://oauth2.googleapis.com/token",
      "https://playintegrity.googleapis.com/v1/com.mustafakoceerr.justrelax:decodeIntegrityToken",
      "https://playintegrity.googleapis.com/v1/com.mustafakoceerr.justrelax:decodeIntegrityToken",
    ]);
    expect(new Headers(calls[1]?.init?.headers).get("authorization")).toBe("Bearer access-1");
    expect(JSON.parse(String(calls[1]?.init?.body))).toEqual({ integrity_token: "token-a" });

    const assertion: string = new URLSearchParams(String(calls[0]?.init?.body)).get("assertion") ?? "";
    const [header, claims, signature] = assertion.split(".") as [string, string, string];
    const signed: boolean = await crypto.subtle.verify(
      "RSASSA-PKCS1-v1_5",
      publicKey,
      base64UrlDecode(signature) ?? new Uint8Array(),
      new TextEncoder().encode(`${header}.${claims}`),
    );
    expect(signed).toBe(true);
    expect(JSON.parse(new TextDecoder().decode(base64UrlDecode(claims) ?? new Uint8Array()))).toEqual({
      iss: "integrity@project.iam.gserviceaccount.com",
      scope: "https://www.googleapis.com/auth/playintegrity",
      aud: "https://oauth2.googleapis.com/token",
      iat: 1_800_000_000,
      exp: 1_800_003_600,
    });
  });

  it("gives nothing when Google refuses", async () => {
    const { account } = await serviceAccount();
    const refusing = async (): Promise<Response> => new Response("no", { status: 403 });
    const decoder = new GoogleIntegrityDecoder(account, "com.mustafakoceerr.justrelax", refusing, (): number => 0);

    expect(await decoder.decode("token")).toBeNull();
  });

  it("does not accept a service account without a key", () => {
    expect(parseServiceAccount("{}")).toBeNull();
    expect(parseServiceAccount("not json")).toBeNull();
  });
});
