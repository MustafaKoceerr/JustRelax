import { buildSearchUrl, toSearchPage, type SearchPage } from "./freesound";
import { evaluateVerdict, sessionRequestHash, type VerdictResult } from "./integrity";
import { OPENAI_CHAT_URL, buildMixChatRequest, describeUsage, parseMixCompletion, type Mix } from "./openai";
import { SESSION_TTL_MS, issueSession, verifySession } from "./session";
import {
  parseMixRequest,
  parseSearchQuery,
  parseSessionRequest,
  type MixRequest,
  type SearchQuery,
  type SessionRequest,
} from "./validate";

export interface Env {
  OPENAI_API_KEY: string;
  FREESOUND_API_KEY: string;
  /** Signs session tokens; any long random string. */
  SESSION_SECRET: string;
  GOOGLE_SERVICE_ACCOUNT_JSON: string;
  PACKAGE_NAME: string;
  /** "false" only for local development, where no Play Integrity token exists. */
  INTEGRITY_REQUIRED: string;
  SESSION_LIMITER: RateLimit;
  /** Per install; AI_IP_LIMITER and AI_GLOBAL_LIMITER cap what new install ids could add. */
  AI_LIMITER: RateLimit;
  AI_IP_LIMITER: RateLimit;
  AI_GLOBAL_LIMITER: RateLimit;
  SEARCH_LIMITER: RateLimit;
}

export interface Deps {
  fetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;
  now: () => number;
  /** Decodes a Play Integrity token on Google's servers; null if it cannot be decoded. */
  decodeIntegrity: (integrityToken: string) => Promise<unknown>;
}

const INTEGRITY_MAX_AGE_MS: number = 10 * 60 * 1_000;

export async function handle(request: Request, env: Env, deps: Deps): Promise<Response> {
  const url: URL = new URL(request.url);
  switch (url.pathname) {
    case "/v1/session":
      return request.method === "POST" ? createSession(request, env, deps) : error(405, "method_not_allowed");
    case "/v1/ai/mix":
      return request.method === "POST" ? aiMix(request, env, deps) : error(405, "method_not_allowed");
    case "/v1/freesound/search":
      return request.method === "GET" ? freesoundSearch(request, url, env, deps) : error(405, "method_not_allowed");
    default:
      return error(404, "not_found");
  }
}

async function createSession(request: Request, env: Env, deps: Deps): Promise<Response> {
  const ip: string = request.headers.get("cf-connecting-ip") ?? "unknown";
  if (!(await env.SESSION_LIMITER.limit({ key: `ip:${ip}` })).success) return error(429, "rate_limited");

  const body: SessionRequest | null = parseSessionRequest(await readJson(request));
  if (body === null) return error(400, "bad_request");

  if (env.INTEGRITY_REQUIRED !== "false") {
    const verdict: unknown = await deps.decodeIntegrity(body.integrityToken);
    if (verdict === null) return error(503, "integrity_unavailable");
    const result: VerdictResult = evaluateVerdict(verdict, await sessionRequestHash(body.installId), deps.now(), {
      packageName: env.PACKAGE_NAME,
      maxAgeMs: INTEGRITY_MAX_AGE_MS,
    });
    if (!result.ok) return error(403, `integrity_${result.reason}`);
  }

  const now: number = deps.now();
  return Response.json({ token: await issueSession(body.installId, now, env.SESSION_SECRET), expiresAt: now + SESSION_TTL_MS });
}

async function aiMix(request: Request, env: Env, deps: Deps): Promise<Response> {
  const installId: string | null = await authenticate(request, env, deps);
  if (installId === null) return error(401, "unauthorized");
  const ip: string = request.headers.get("cf-connecting-ip") ?? "unknown";
  const allowed: boolean =
    (await env.AI_LIMITER.limit({ key: `install:${installId}` })).success &&
    (await env.AI_IP_LIMITER.limit({ key: `ip:${ip}` })).success &&
    (await env.AI_GLOBAL_LIMITER.limit({ key: "global" })).success;
  if (!allowed) return error(429, "rate_limited");

  const mixRequest: MixRequest | null = parseMixRequest(await readJson(request));
  if (mixRequest === null) return error(400, "bad_request");

  const response: Response = await deps.fetch(OPENAI_CHAT_URL, {
    method: "POST",
    headers: { authorization: `Bearer ${env.OPENAI_API_KEY}`, "content-type": "application/json" },
    body: JSON.stringify(buildMixChatRequest(mixRequest)),
  });
  if (!response.ok) {
    // Only the status: the prompt is the user's text and is never logged.
    console.error(`OpenAI answered ${response.status}`);
    return error(502, "upstream_error");
  }
  const completion: unknown = await readJsonResponse(response);
  const usage: string | null = describeUsage(completion);
  if (usage !== null) console.log(usage);
  const mix: Mix | null = parseMixCompletion(completion, mixRequest);
  return mix === null ? error(502, "upstream_error") : Response.json(mix);
}

async function freesoundSearch(request: Request, url: URL, env: Env, deps: Deps): Promise<Response> {
  const installId: string | null = await authenticate(request, env, deps);
  if (installId === null) return error(401, "unauthorized");
  if (!(await env.SEARCH_LIMITER.limit({ key: `install:${installId}` })).success) return error(429, "rate_limited");

  const search: SearchQuery | null = parseSearchQuery(url);
  if (search === null) return error(400, "bad_request");

  const response: Response = await deps.fetch(buildSearchUrl(search), {
    headers: { authorization: `Token ${env.FREESOUND_API_KEY}` },
  });
  if (response.status === 429) return error(429, "rate_limited");
  if (!response.ok) return error(502, "upstream_error");
  const page: SearchPage | null = toSearchPage(await readJsonResponse(response));
  return page === null ? error(502, "upstream_error") : Response.json(page);
}

/** The install id of a valid `Authorization: Bearer <session>` header. */
async function authenticate(request: Request, env: Env, deps: Deps): Promise<string | null> {
  const header: string | null = request.headers.get("authorization");
  if (header === null || !header.startsWith("Bearer ")) return null;
  return verifySession(header.slice("Bearer ".length), deps.now(), env.SESSION_SECRET);
}

async function readJson(request: Request): Promise<unknown> {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

async function readJsonResponse(response: Response): Promise<unknown> {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

function error(status: number, code: string): Response {
  return Response.json({ error: code }, { status });
}
