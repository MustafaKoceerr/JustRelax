import { describe, expect, it } from "vitest";
import { decodeCbor } from "../src/cbor";

function bytes(...values: number[]): Uint8Array {
  return Uint8Array.from(values);
}

describe("decodeCbor", (): void => {
  it("reads integers of every width", (): void => {
    expect(decodeCbor(bytes(0x0a))).toBe(10);
    expect(decodeCbor(bytes(0x18, 0xff))).toBe(255);
    expect(decodeCbor(bytes(0x19, 0x01, 0x00))).toBe(256);
    expect(decodeCbor(bytes(0x1a, 0x00, 0x01, 0x00, 0x00))).toBe(65_536);
    expect(decodeCbor(bytes(0x20))).toBe(-1);
  });

  it("reads byte and text strings", (): void => {
    expect(decodeCbor(bytes(0x43, 1, 2, 3))).toEqual(bytes(1, 2, 3));
    expect(decodeCbor(bytes(0x63, 0x66, 0x6d, 0x74))).toBe("fmt");
  });

  it("reads arrays and maps with text keys", (): void => {
    // {"a": [1, h'ff'], "b": true}
    const decoded: unknown = decodeCbor(bytes(0xa2, 0x61, 0x61, 0x82, 0x01, 0x41, 0xff, 0x61, 0x62, 0xf5));
    expect(decoded).toEqual({ a: [1, bytes(0xff)], b: true });
  });

  it("refuses truncated input, trailing bytes and indefinite lengths", (): void => {
    expect(decodeCbor(bytes(0x43, 1, 2))).toBeNull();
    expect(decodeCbor(bytes(0x01, 0x02))).toBeNull();
    expect(decodeCbor(bytes(0x5f, 0x41, 0x01, 0xff))).toBeNull();
    expect(decodeCbor(bytes())).toBeNull();
  });

  it("refuses maps whose keys are not text", (): void => {
    expect(decodeCbor(bytes(0xa1, 0x01, 0x02))).toBeNull();
  });
});
