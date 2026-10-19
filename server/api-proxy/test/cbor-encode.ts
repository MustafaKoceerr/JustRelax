/** A CBOR encoder for the shapes the tests rebuild: maps with text keys, arrays, bytes, text, unsigned ints. */
export function encodeCbor(value: unknown): Uint8Array {
  const out: number[] = [];
  write(value, out);
  return Uint8Array.from(out);
}

function head(major: number, length: number, out: number[]): void {
  if (length < 24) out.push((major << 5) | length);
  else if (length < 0x100) out.push((major << 5) | 24, length);
  else if (length < 0x10000) out.push((major << 5) | 25, length >> 8, length & 0xff);
  else out.push((major << 5) | 26, (length >>> 24) & 0xff, (length >> 16) & 0xff, (length >> 8) & 0xff, length & 0xff);
}

function write(value: unknown, out: number[]): void {
  if (typeof value === "number") head(0, value, out);
  else if (typeof value === "string") {
    const bytes: Uint8Array = new TextEncoder().encode(value);
    head(3, bytes.length, out);
    out.push(...bytes);
  } else if (value instanceof Uint8Array) {
    head(2, value.length, out);
    for (const byte of value) out.push(byte);
  } else if (Array.isArray(value)) {
    head(4, value.length, out);
    for (const item of value) write(item, out);
  } else if (typeof value === "object" && value !== null) {
    const entries: [string, unknown][] = Object.entries(value);
    head(5, entries.length, out);
    for (const [key, item] of entries) {
      write(key, out);
      write(item, out);
    }
  } else throw new Error(`Cannot encode ${String(value)}`);
}
