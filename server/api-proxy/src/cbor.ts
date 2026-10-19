/**
 * Decodes the small part of CBOR (RFC 8949) that App Attest objects use: unsigned and negative
 * integers, byte and text strings, arrays, maps with text keys and true/false/null, all with
 * definite lengths. Anything else, a truncated input or bytes left over gives null.
 */
export function decodeCbor(bytes: Uint8Array): unknown {
  const reader: Reader = { bytes, offset: 0 };
  try {
    const value: unknown = readItem(reader, 0);
    return reader.offset === bytes.length ? value : null;
  } catch {
    return null;
  }
}

interface Reader {
  bytes: Uint8Array;
  offset: number;
}

const MAX_DEPTH: number = 16;

function readItem(reader: Reader, depth: number): unknown {
  if (depth > MAX_DEPTH) throw new Error("too deep");
  const initial: number = readByte(reader);
  const major: number = initial >> 5;
  const info: number = initial & 0x1f;

  if (major === 7) {
    if (info === 20) return false;
    if (info === 21) return true;
    if (info === 22) return null;
    throw new Error("unsupported simple value");
  }

  const argument: number = readArgument(reader, info);
  switch (major) {
    case 0:
      return argument;
    case 1:
      return -1 - argument;
    case 2:
      return readBytes(reader, argument).slice();
    case 3:
      return new TextDecoder("utf-8", { fatal: true, ignoreBOM: false }).decode(readBytes(reader, argument));
    case 4: {
      const items: unknown[] = [];
      for (let i: number = 0; i < argument; i++) items.push(readItem(reader, depth + 1));
      return items;
    }
    case 5: {
      // No prototype, so a "__proto__" key is just a key.
      const map: Record<string, unknown> = Object.create(null) as Record<string, unknown>;
      for (let i: number = 0; i < argument; i++) {
        const key: unknown = readItem(reader, depth + 1);
        if (typeof key !== "string") throw new Error("map key is not text");
        map[key] = readItem(reader, depth + 1);
      }
      return map;
    }
    default:
      throw new Error("unsupported major type");
  }
}

function readArgument(reader: Reader, info: number): number {
  if (info < 24) return info;
  if (info === 24) return readByte(reader);
  if (info === 25) return (readByte(reader) << 8) | readByte(reader);
  if (info === 26) return ((readByte(reader) << 24) >>> 0) + ((readByte(reader) << 16) | (readByte(reader) << 8) | readByte(reader));
  // 64-bit lengths and indefinite lengths (31) never appear in App Attest objects.
  throw new Error("unsupported length");
}

function readByte(reader: Reader): number {
  const byte: number | undefined = reader.bytes[reader.offset];
  if (byte === undefined) throw new Error("truncated");
  reader.offset++;
  return byte;
}

function readBytes(reader: Reader, length: number): Uint8Array {
  if (reader.offset + length > reader.bytes.length) throw new Error("truncated");
  const slice: Uint8Array = reader.bytes.subarray(reader.offset, reader.offset + length);
  reader.offset += length;
  return slice;
}
