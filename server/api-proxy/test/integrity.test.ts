import { describe, expect, it } from "vitest";
import { evaluateVerdict, sessionRequestHash, type IntegrityPolicy } from "../src/integrity";

const POLICY: IntegrityPolicy = { packageName: "com.mustafakoceerr.justrelax", maxAgeMs: 600_000 };
const NOW: number = 1_800_000_000_000;

function verdict(overrides: Record<string, unknown> = {}): unknown {
  return {
    requestDetails: {
      requestPackageName: "com.mustafakoceerr.justrelax",
      requestHash: "abc",
      timestampMillis: String(NOW - 5_000),
    },
    appIntegrity: { appRecognitionVerdict: "PLAY_RECOGNIZED", packageName: "com.mustafakoceerr.justrelax" },
    deviceIntegrity: { deviceRecognitionVerdict: ["MEETS_DEVICE_INTEGRITY"] },
    ...overrides,
  };
}

describe("evaluateVerdict", () => {
  it("accepts the genuine app on a genuine device for this request", () => {
    expect(evaluateVerdict(verdict(), "abc", NOW, POLICY)).toEqual({ ok: true });
  });

  it("rejects a token made for another request or another app", () => {
    expect(evaluateVerdict(verdict(), "other-hash", NOW, POLICY)).toEqual({ ok: false, reason: "request_hash" });
    expect(
      evaluateVerdict(
        verdict({ requestDetails: { requestPackageName: "evil.app", requestHash: "abc", timestampMillis: String(NOW) } }),
        "abc",
        NOW,
        POLICY,
      ),
    ).toEqual({ ok: false, reason: "package" });
  });

  it("rejects an old token", () => {
    const old: unknown = verdict({
      requestDetails: {
        requestPackageName: "com.mustafakoceerr.justrelax",
        requestHash: "abc",
        timestampMillis: String(NOW - 600_001),
      },
    });

    expect(evaluateVerdict(old, "abc", NOW, POLICY)).toEqual({ ok: false, reason: "stale" });
  });

  it("rejects a copy not from Play and a device that fails integrity", () => {
    expect(
      evaluateVerdict(verdict({ appIntegrity: { appRecognitionVerdict: "UNRECOGNIZED_VERSION" } }), "abc", NOW, POLICY),
    ).toEqual({ ok: false, reason: "app" });
    expect(evaluateVerdict(verdict({ deviceIntegrity: {} }), "abc", NOW, POLICY)).toEqual({ ok: false, reason: "device" });
  });

  it("rejects a payload that is not a verdict", () => {
    expect(evaluateVerdict("nonsense", "abc", NOW, POLICY)).toEqual({ ok: false, reason: "malformed" });
  });
});

describe("sessionRequestHash", () => {
  it("is the hex SHA-256 of the install id with the session prefix, as the app computes it", async () => {
    // Same vector as the app's IntegrityRequestHashTest.
    expect(await sessionRequestHash("install-1")).toBe("b19f76277ff87a14d93396b5552ae5b71806ab94625a5a44fd0db9a917eebee6");
  });
});
