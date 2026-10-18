import { describe, expect, it } from "vitest";
import { issueSession, verifySession } from "../src/session";

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
