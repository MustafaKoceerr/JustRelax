import { describe, expect, it } from "vitest";
import { issueKeyToken, issueSession, verifyKeyToken, verifySession, type AttestedKey } from "../src/session";

const SECRET: string = "test-secret-with-enough-entropy";

describe("session tokens", () => {
  it("a token issued for an install verifies until it expires", async () => {
    const token: string = await issueSession("install-1", 1_000, SECRET);

    expect(await verifySession(token, 1_000 + 3_599_000, SECRET)).toBe("install-1");
    expect(await verifySession(token, 1_000 + 3_600_001, SECRET)).toBeNull();
  });

  it("a token signed with another secret or changed in transit is rejected", async () => {
    const token: string = await issueSession("install-1", 1_000, SECRET);
    const [payload, signature] = token.split(".") as [string, string];
    const forged: string = `${btoa(JSON.stringify({ sub: "install-2", exp: 9_999_999 })).replace(/=+$/, "")}.${signature}`;

    expect(await verifySession(token, 2_000, "other-secret")).toBeNull();
    expect(await verifySession(forged, 2_000, SECRET)).toBeNull();
    expect(await verifySession(`${payload}.`, 2_000, SECRET)).toBeNull();
    expect(await verifySession("not-a-token", 2_000, SECRET)).toBeNull();
  });
});

describe("App Attest key tokens", () => {
  const key: AttestedKey = { installId: "install-1", keyId: "a2V5LWlk", publicKey: Uint8Array.from([4, 1, 2, 3]) };

  it("carries the attested key back unchanged", async () => {
    const token: string = await issueKeyToken(key, SECRET);

    expect(await verifyKeyToken(token, SECRET)).toEqual(key);
  });

  it("is refused with another secret, and is never mistaken for a session or the other way round", async () => {
    const token: string = await issueKeyToken(key, SECRET);

    expect(await verifyKeyToken(token, "other-secret")).toBeNull();
    expect(await verifySession(token, 2_000, SECRET)).toBeNull();
    expect(await verifyKeyToken(await issueSession("install-1", 1_000, SECRET), SECRET)).toBeNull();
  });
});
