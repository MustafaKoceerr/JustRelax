import { beforeEach, describe, expect, it } from "vitest";
import { handle, type Deps, type Env } from "../src/handler";
import type { AssertionResult, AttestationResult } from "../src/appattest";
import { issueKeyToken, issueSession } from "../src/session";
import { PACKAGE_NAME, verdict } from "./fixtures";

const NOW: number = 1_800_000_000_000;
const SECRET: string = "session-secret";
const INSTALL: string = "1b4e28ba-2fa1-11d2-883f-0016d3cca427";
const APP_ID: string = "TEAM123456.com.mustafakoceerr.justrelax";
const PUBLIC_KEY: Uint8Array = Uint8Array.from([4, 9, 9, 9]);

class FakeLimiter implements RateLimit {
  readonly keys: string[] = [];
  allow: boolean = true;
  async limit(options: RateLimitOptions): Promise<RateLimitOutcome> {
    this.keys.push(options.key);
    return { success: this.allow };
  }
}

interface Upstream {
  url: string;
  headers: Headers;
  body: string | null;
}

let env: Env;
let upstream: Upstream[];
let upstreamResponse: Response;
let decoded: unknown;
let deps: Deps;
let attestation: AttestationResult;
let assertion: AssertionResult;
let appAttestCalls: { kind: string; clientData: string; keyId?: string; publicKey?: number[]; appId: string; allowDevelopment?: boolean }[];

function fakeEnv(): Env {
  return {
    OPENAI_API_KEY: "sk-test",
    FREESOUND_API_KEY: "fs-test",
    SESSION_SECRET: SECRET,
    GOOGLE_SERVICE_ACCOUNT_JSON: "{}",
    PACKAGE_NAME,
    APPLE_APP_ID: APP_ID,
    APP_ATTEST_ALLOW_DEVELOPMENT: "false",
    INTEGRITY_REQUIRED: "true",
    SESSION_LIMITER: new FakeLimiter(),
    AI_LIMITER: new FakeLimiter(),
    AI_IP_LIMITER: new FakeLimiter(),
    AI_GLOBAL_LIMITER: new FakeLimiter(),
    SEARCH_LIMITER: new FakeLimiter(),
  };
}

beforeEach((): void => {
  env = fakeEnv();
  upstream = [];
  upstreamResponse = Response.json({});
  decoded = null;
  deps = {
    now: (): number => NOW,
    fetch: async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      upstream.push({ url: String(input), headers: new Headers(init?.headers), body: typeof init?.body === "string" ? init.body : null });
      return upstreamResponse;
    },
    decodeIntegrity: async (): Promise<unknown> => decoded,
    appAttest: {
      verifyAttestation: async (
        _: Uint8Array,
        clientData: Uint8Array,
        keyId: string,
        appId: string,
        __: number,
        allowDevelopment: boolean,
      ): Promise<AttestationResult> => {
        appAttestCalls.push({ kind: "attestation", clientData: new TextDecoder().decode(clientData), keyId, appId, allowDevelopment });
        return attestation;
      },
      verifyAssertion: async (_: Uint8Array, clientData: Uint8Array, publicKey: Uint8Array, appId: string): Promise<AssertionResult> => {
        appAttestCalls.push({ kind: "assertion", clientData: new TextDecoder().decode(clientData), publicKey: [...publicKey], appId });
        return assertion;
      },
    },
  };
  attestation = { ok: true, publicKey: PUBLIC_KEY };
  assertion = { ok: true, counter: 2 };
  appAttestCalls = [];
});

async function session(): Promise<string> {
  return issueSession(INSTALL, NOW, SECRET);
}

function request(path: string, init: RequestInit = {}): Request {
  return new Request(`https://api.test${path}`, {
    ...init,
    headers: { "cf-connecting-ip": "203.0.113.7", ...(init.headers as Record<string, string> | undefined) },
  });
}

/** A request from an app with a valid session. */
async function authed(path: string, init: RequestInit = {}): Promise<Request> {
  return request(path, { ...init, headers: { authorization: `Bearer ${await session()}` } });
}

async function json(response: Response): Promise<unknown> {
  return response.json();
}

function genuineVerdict(hash: string): unknown {
  return verdict(hash, NOW - 1_000);
}

const SESSION_HASH: string = "b19f76277ff87a14d93396b5552ae5b71806ab94625a5a44fd0db9a917eebee6";

describe("POST /v1/session", () => {
  const install1: string = "install-1";

  it("issues an hour-long session to the genuine app", async () => {
    decoded = genuineVerdict(SESSION_HASH);

    const response: Response = await handle(
      request("/v1/session", { method: "POST", body: JSON.stringify({ installId: install1, integrityToken: "tok" }) }),
      env,
      deps,
    );

    expect(response.status).toBe(200);
    const body = (await json(response)) as { token: string; expiresAt: number };
    expect(typeof body.token).toBe("string");
    expect(body.expiresAt).toBe(NOW + 3_600_000);
    expect((env.SESSION_LIMITER as FakeLimiter).keys).toEqual(["ip:203.0.113.7"]);
  });

  it("refuses a failing verdict, an unreachable Google and a bad body", async () => {
    decoded = verdict(SESSION_HASH, NOW - 1_000, { appIntegrity: { appRecognitionVerdict: "UNEVALUATED" } });
    const refused: Response = await handle(
      request("/v1/session", { method: "POST", body: JSON.stringify({ installId: install1, integrityToken: "tok" }) }),
      env,
      deps,
    );
    expect(refused.status).toBe(403);
    expect(await json(refused)).toEqual({ error: "integrity_app" });

    decoded = null;
    const unavailable: Response = await handle(
      request("/v1/session", { method: "POST", body: JSON.stringify({ installId: install1, integrityToken: "tok" }) }),
      env,
      deps,
    );
    expect(unavailable.status).toBe(503);

    const bad: Response = await handle(request("/v1/session", { method: "POST", body: "{" }), env, deps);
    expect(bad.status).toBe(400);
  });

  it("skips the integrity check only when the environment turns it off (local development)", async () => {
    env.INTEGRITY_REQUIRED = "false";

    const response: Response = await handle(
      request("/v1/session", { method: "POST", body: JSON.stringify({ installId: install1, integrityToken: "none" }) }),
      env,
      deps,
    );

    expect(response.status).toBe(200);
  });

  it("is rate limited per IP", async () => {
    (env.SESSION_LIMITER as FakeLimiter).allow = false;

    const response: Response = await handle(
      request("/v1/session", { method: "POST", body: JSON.stringify({ installId: install1, integrityToken: "tok" }) }),
      env,
      deps,
    );

    expect(response.status).toBe(429);
  });
});

describe("POST /v1/session from iOS (App Attest)", () => {
  const install: string = "install-1";
  const keyId: string = "s/134MbeEEZDZKCvOTf+jZgNhpoDwdXZ8cKfTym8FUg=";

  function appAttestToken(fields: Record<string, unknown>): string {
    return `appattest.v1.${btoa(JSON.stringify(fields)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "")}`;
  }

  async function createSession(fields: Record<string, unknown>): Promise<Response> {
    return handle(
      request("/v1/session", { method: "POST", body: JSON.stringify({ installId: install, integrityToken: appAttestToken(fields) }) }),
      env,
      deps,
    );
  }

  it("checks a new key's attestation over the session hash and time, then returns a session and the key token", async () => {
    const response: Response = await createSession({ keyId, timestampMs: NOW - 5_000, attestation: btoa("attestation") });

    expect(response.status).toBe(200);
    const body = (await json(response)) as { token: string; expiresAt: number; keyToken: string };
    expect(body.expiresAt).toBe(NOW + 3_600_000);
    expect(typeof body.keyToken).toBe("string");
    expect(appAttestCalls).toEqual([
      { kind: "attestation", clientData: `${SESSION_HASH}:${NOW - 5_000}`, keyId, appId: APP_ID, allowDevelopment: false },
    ]);
  });

  it("later accepts an assertion signed by the attested key", async () => {
    const keyToken: string = await issueKeyToken({ installId: install, keyId, publicKey: PUBLIC_KEY }, SECRET);

    const response: Response = await createSession({ keyId, timestampMs: NOW, assertion: btoa("assertion"), keyToken });

    expect(response.status).toBe(200);
    const body = (await json(response)) as { token: string; keyToken?: string };
    expect(typeof body.token).toBe("string");
    expect(body.keyToken).toBeUndefined();
    expect(appAttestCalls).toEqual([{ kind: "assertion", clientData: `${SESSION_HASH}:${NOW}`, publicKey: [...PUBLIC_KEY], appId: APP_ID }]);
  });

  it("refuses a failed check, another install's key, an old timestamp and a malformed token", async () => {
    attestation = { ok: false, reason: "chain" };
    const failed: Response = await createSession({ keyId, timestampMs: NOW, attestation: btoa("attestation") });
    expect(failed.status).toBe(403);
    expect(await json(failed)).toEqual({ error: "integrity_chain" });

    const foreignKey: string = await issueKeyToken({ installId: "install-2", keyId, publicKey: PUBLIC_KEY }, SECRET);
    const stolen: Response = await createSession({ keyId, timestampMs: NOW, assertion: btoa("assertion"), keyToken: foreignKey });
    expect(await json(stolen)).toEqual({ error: "integrity_key_token" });

    const stale: Response = await createSession({ keyId, timestampMs: NOW - 11 * 60_000, attestation: btoa("attestation") });
    expect(await json(stale)).toEqual({ error: "integrity_stale" });

    const fromTheFuture: Response = await createSession({ keyId, timestampMs: NOW + 2 * 60_000, attestation: btoa("attestation") });
    expect(await json(fromTheFuture)).toEqual({ error: "integrity_stale" });

    const malformed: Response = await createSession({ keyId });
    expect(await json(malformed)).toEqual({ error: "integrity_malformed" });
    expect(appAttestCalls.length).toBe(1);
  });

  it("passes development keys through only where the environment allows them", async () => {
    env.APP_ATTEST_ALLOW_DEVELOPMENT = "true";

    await createSession({ keyId, timestampMs: NOW, attestation: btoa("attestation") });

    expect(appAttestCalls[0]?.allowDevelopment).toBe(true);
  });

  it("is unavailable until the App ID is configured", async () => {
    env.APPLE_APP_ID = "";

    const response: Response = await createSession({ keyId, timestampMs: NOW, attestation: btoa("attestation") });

    expect(response.status).toBe(503);
    expect(await json(response)).toEqual({ error: "appattest_unavailable" });
  });
});

describe("POST /v1/ai/mix", () => {
  const body: string = JSON.stringify({
    prompt: "rain",
    sounds: [{ id: "rain_light", name: "Light Rain", category: "RAIN", kind: "BED", tags: [] }],
  });

  it("needs a valid session", async () => {
    const missing: Response = await handle(request("/v1/ai/mix", { method: "POST", body }), env, deps);
    const wrong: Response = await handle(
      request("/v1/ai/mix", { method: "POST", body, headers: { authorization: "Bearer forged.token" } }),
      env,
      deps,
    );

    expect(missing.status).toBe(401);
    expect(wrong.status).toBe(401);
    expect(upstream).toEqual([]);
  });

  it("asks OpenAI with the server key and returns the checked mix", async () => {
    upstreamResponse = Response.json({
      choices: [{ message: { content: JSON.stringify({ mix_name: "Rain", description: "d", sounds: [{ id: "rain_light", volume: 0.6 }] }) } }],
    });

    const response: Response = await handle(
      await authed("/v1/ai/mix", { method: "POST", body }),
      env,
      deps,
    );

    expect(response.status).toBe(200);
    expect(await json(response)).toEqual({ mix_name: "Rain", description: "d", sounds: [{ id: "rain_light", volume: 0.6 }] });
    expect(upstream[0]?.url).toBe("https://api.openai.com/v1/chat/completions");
    expect(upstream[0]?.headers.get("authorization")).toBe("Bearer sk-test");
    expect((env.AI_LIMITER as FakeLimiter).keys).toEqual([`install:${INSTALL}`]);
    expect((env.AI_IP_LIMITER as FakeLimiter).keys).toEqual(["ip:203.0.113.7"]);
    expect((env.AI_GLOBAL_LIMITER as FakeLimiter).keys).toEqual(["global"]);
  });

  it("is also limited per IP and for everyone together, so new install ids do not raise the limit", async () => {
    for (const limiter of [env.AI_IP_LIMITER, env.AI_GLOBAL_LIMITER] as FakeLimiter[]) {
      (env.AI_IP_LIMITER as FakeLimiter).allow = true;
      (env.AI_GLOBAL_LIMITER as FakeLimiter).allow = true;
      limiter.allow = false;

      const response: Response = await handle(
        await authed("/v1/ai/mix", { method: "POST", body }),
        env,
        deps,
      );

      expect(response.status).toBe(429);
    }
    expect(upstream).toEqual([]);
  });

  it("is rate limited per install and reports an OpenAI failure as a bad gateway", async () => {
    (env.AI_LIMITER as FakeLimiter).allow = false;
    const limited: Response = await handle(
      await authed("/v1/ai/mix", { method: "POST", body }),
      env,
      deps,
    );
    expect(limited.status).toBe(429);
    expect(upstream).toEqual([]);

    (env.AI_LIMITER as FakeLimiter).allow = true;
    upstreamResponse = new Response("boom", { status: 500 });
    const failed: Response = await handle(
      await authed("/v1/ai/mix", { method: "POST", body }),
      env,
      deps,
    );
    expect(failed.status).toBe(502);
  });
});

describe("GET /v1/freesound/search", () => {
  it("searches with the server key in the header and returns the page", async () => {
    upstreamResponse = Response.json({ count: 0, next: null, results: [] });

    const response: Response = await handle(
      await authed("/v1/freesound/search?query=rain&page=2"),
      env,
      deps,
    );

    expect(response.status).toBe(200);
    expect(await json(response)).toEqual({ count: 0, next: null, results: [] });
    expect(upstream[0]?.headers.get("authorization")).toBe("Token fs-test");
    expect(new URL(upstream[0]?.url ?? "").searchParams.get("page")).toBe("2");
    expect((env.SEARCH_LIMITER as FakeLimiter).keys).toEqual([`install:${INSTALL}`]);
  });

  it("passes Freesound's rate limit on and rejects a bad query", async () => {
    upstreamResponse = new Response("slow down", { status: 429 });
    const throttled: Response = await handle(
      await authed("/v1/freesound/search?query=rain"),
      env,
      deps,
    );
    expect(throttled.status).toBe(429);

    const bad: Response = await handle(
      await authed("/v1/freesound/search?query="),
      env,
      deps,
    );
    expect(bad.status).toBe(400);
  });
});

describe("routing", () => {
  it("answers unknown paths with 404 and wrong methods with 405", async () => {
    expect((await handle(request("/v1/nope"), env, deps)).status).toBe(404);
    expect((await handle(request("/v1/session"), env, deps)).status).toBe(405);
  });
});
