import { GoogleIntegrityDecoder, parseServiceAccount, type ServiceAccount } from "./google";
import { handle, type Env } from "./handler";

/** One decoder per isolate, so its Google access token is reused across requests. */
let decoder: GoogleIntegrityDecoder | null = null;

function integrityDecoder(env: Env): GoogleIntegrityDecoder | null {
  if (decoder !== null) return decoder;
  const account: ServiceAccount | null = parseServiceAccount(env.GOOGLE_SERVICE_ACCOUNT_JSON);
  if (account === null) return null;
  decoder = new GoogleIntegrityDecoder(
    account,
    env.PACKAGE_NAME,
    (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => fetch(input, init),
    Date.now,
  );
  return decoder;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    return handle(request, env, {
      fetch: (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => fetch(input, init),
      now: Date.now,
      decodeIntegrity: async (token: string): Promise<unknown> => (await integrityDecoder(env)?.decode(token)) ?? null,
    });
  },
} satisfies ExportedHandler<Env>;
