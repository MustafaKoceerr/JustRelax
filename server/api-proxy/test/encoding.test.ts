import { describe, expect, it } from "vitest";
import { base64UrlDecode, base64UrlEncode, isRecord, sha256Hex } from "../src/encoding";

describe("base64url", () => {
  it("round-trips every padding length without padding or + and /", () => {
    for (const length of [0, 1, 2, 3, 4, 5]) {
      const bytes: Uint8Array = Uint8Array.from({ length }, (_: unknown, i: number): number => 250 + i);
      const text: string = base64UrlEncode(bytes);

      expect(text).toMatch(/^[A-Za-z0-9_-]*$/);
      expect(base64UrlDecode(text)).toEqual(bytes);
    }
  });

  it("uses the url-safe alphabet", () => {
    expect(base64UrlEncode(Uint8Array.of(0xfb, 0xff))).toBe("-_8");
  });

  it("rejects text outside the alphabet or with an impossible length", () => {
    expect(base64UrlDecode("ab+c")).toBeNull();
    expect(base64UrlDecode("abc=")).toBeNull();
    expect(base64UrlDecode("a")).toBeNull();
  });
});

describe("sha256Hex", () => {
  it("matches the known digest", async () => {
    expect(await sha256Hex("abc")).toBe("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
  });
});

describe("isRecord", () => {
  it("accepts plain objects only", () => {
    expect(isRecord({})).toBe(true);
    expect(isRecord([])).toBe(false);
    expect(isRecord(null)).toBe(false);
    expect(isRecord("x")).toBe(false);
  });
});
