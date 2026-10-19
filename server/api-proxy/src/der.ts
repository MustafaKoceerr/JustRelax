/**
 * Just enough DER (X.690) to check an App Attest certificate chain with WebCrypto: the signed
 * part, the signature and its algorithm, the validity, the public key and the extensions. Every
 * function throws on input it does not understand; callers turn that into a refusal.
 */

export interface Element {
  tag: number;
  /** The whole element, header included. */
  raw: Uint8Array;
  /** The contents only. */
  value: Uint8Array;
}

export interface Certificate {
  /** The `tbsCertificate` bytes the issuer signed. */
  signed: Uint8Array;
  signatureAlgorithm: string;
  /** DER `ECDSA-Sig-Value`. */
  signature: Uint8Array;
  notBefore: number;
  notAfter: number;
  /** The whole `SubjectPublicKeyInfo`, as WebCrypto imports it. */
  spki: Uint8Array;
  /** The key's curve OID. */
  curve: string;
  /** The key itself: for EC, the uncompressed point. */
  publicKey: Uint8Array;
  /** Extension values (the OCTET STRING contents) by OID. */
  extensions: Map<string, Uint8Array>;
}

export const TAG_INTEGER: number = 0x02;
export const TAG_BIT_STRING: number = 0x03;
export const TAG_OCTET_STRING: number = 0x04;
export const TAG_OID: number = 0x06;
export const TAG_SEQUENCE: number = 0x30;
const TAG_UTC_TIME: number = 0x17;
const TAG_GENERALIZED_TIME: number = 0x18;
const TAG_BOOLEAN: number = 0x01;

/** The elements one after another in [bytes]. */
export function readElements(bytes: Uint8Array): Element[] {
  const elements: Element[] = [];
  let offset: number = 0;
  while (offset < bytes.length) {
    const element: Element = readElement(bytes, offset);
    elements.push(element);
    offset += element.raw.length;
  }
  return elements;
}

/** The single element that makes up [bytes]. */
export function readSingle(bytes: Uint8Array, tag: number): Element {
  const elements: Element[] = readElements(bytes);
  if (elements.length !== 1) throw new Error("expected one element");
  return expectTag(elements[0]!, tag);
}

export function expectTag(element: Element, tag: number): Element {
  if (element.tag !== tag) throw new Error(`expected tag ${tag}, got ${element.tag}`);
  return element;
}

function readElement(bytes: Uint8Array, start: number): Element {
  const tag: number = byteAt(bytes, start);
  // High tag numbers (0x1f) never appear in these certificates.
  if ((tag & 0x1f) === 0x1f) throw new Error("high tag number");
  let offset: number = start + 1;
  let length: number = byteAt(bytes, offset++);
  if (length & 0x80) {
    const count: number = length & 0x7f;
    if (count === 0 || count > 4) throw new Error("unsupported length");
    length = 0;
    for (let i: number = 0; i < count; i++) length = length * 256 + byteAt(bytes, offset++);
  }
  const end: number = offset + length;
  if (end > bytes.length) throw new Error("truncated");
  return { tag, raw: bytes.subarray(start, end), value: bytes.subarray(offset, end) };
}

function byteAt(bytes: Uint8Array, index: number): number {
  const byte: number | undefined = bytes[index];
  if (byte === undefined) throw new Error("truncated");
  return byte;
}

export function readOid(element: Element): string {
  expectTag(element, TAG_OID);
  const bytes: Uint8Array = element.value;
  if (bytes.length === 0) throw new Error("empty OID");
  const first: number = bytes[0]!;
  const parts: number[] = [Math.min(2, Math.floor(first / 40)), first - Math.min(2, Math.floor(first / 40)) * 40];
  let value: number = 0;
  for (let i: number = 1; i < bytes.length; i++) {
    const byte: number = bytes[i]!;
    value = value * 128 + (byte & 0x7f);
    if (!(byte & 0x80)) {
      parts.push(value);
      value = 0;
    }
  }
  return parts.join(".");
}

/** The bits of a BIT STRING with no unused bits, as all keys and signatures here are. */
export function readBitString(element: Element): Uint8Array {
  expectTag(element, TAG_BIT_STRING);
  if (element.value[0] !== 0) throw new Error("unused bits in BIT STRING");
  return element.value.subarray(1);
}

function readTime(element: Element): number {
  const text: string = new TextDecoder().decode(element.value);
  let match: RegExpMatchArray | null;
  if (element.tag === TAG_UTC_TIME && (match = text.match(/^(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})Z$/))) {
    const year: number = Number(match[1]);
    return Date.UTC(year < 50 ? 2000 + year : 1900 + year, Number(match[2]) - 1, Number(match[3]), Number(match[4]), Number(match[5]), Number(match[6]));
  }
  if (element.tag === TAG_GENERALIZED_TIME && (match = text.match(/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})Z$/))) {
    return Date.UTC(Number(match[1]), Number(match[2]) - 1, Number(match[3]), Number(match[4]), Number(match[5]), Number(match[6]));
  }
  throw new Error("unsupported time");
}

export function parseCertificate(der: Uint8Array): Certificate {
  const [tbs, algorithm, signature] = readElements(readSingle(der, TAG_SEQUENCE).value);
  if (tbs === undefined || algorithm === undefined || signature === undefined) throw new Error("not a certificate");
  expectTag(tbs, TAG_SEQUENCE);

  const fields: Element[] = readElements(tbs.value);
  // version [0] EXPLICIT is present in every v3 certificate.
  let index: number = fields[0]?.tag === 0xa0 ? 1 : 0;
  index += 3; // serialNumber, signature, issuer
  const validity: Element[] = readElements(expectTag(fields[index++]!, TAG_SEQUENCE).value);
  index++; // subject
  const spki: Element = expectTag(fields[index++]!, TAG_SEQUENCE);
  const [keyAlgorithm, keyBits] = readElements(spki.value);
  const [, curveOid] = readElements(expectTag(keyAlgorithm!, TAG_SEQUENCE).value);

  const extensions: Map<string, Uint8Array> = new Map();
  for (const field of fields.slice(index)) {
    if (field.tag !== 0xa3) continue; // extensions [3] EXPLICIT
    for (const extension of readElements(readSingle(field.value, TAG_SEQUENCE).value)) {
      const parts: Element[] = readElements(expectTag(extension, TAG_SEQUENCE).value);
      const value: Element = expectTag(parts[parts.length - 1]!, TAG_OCTET_STRING);
      if (parts.length === 3) expectTag(parts[1]!, TAG_BOOLEAN);
      extensions.set(readOid(parts[0]!), value.value);
    }
  }

  return {
    signed: tbs.raw,
    signatureAlgorithm: readOid(readElements(expectTag(algorithm, TAG_SEQUENCE).value)[0]!),
    signature: readBitString(signature),
    notBefore: readTime(validity[0]!),
    notAfter: readTime(validity[1]!),
    spki: spki.raw,
    curve: readOid(curveOid!),
    publicKey: readBitString(keyBits!),
    extensions,
  };
}

/** A DER `ECDSA-Sig-Value` as the fixed-size `r || s` WebCrypto verifies. */
export function ecdsaSignatureToRaw(der: Uint8Array, size: number): Uint8Array {
  const [r, s] = readElements(readSingle(der, TAG_SEQUENCE).value);
  const raw: Uint8Array = new Uint8Array(size * 2);
  [r, s].forEach((part: Element | undefined, i: number): void => {
    let bytes: Uint8Array = expectTag(part!, TAG_INTEGER).value;
    while (bytes.length > size && bytes[0] === 0) bytes = bytes.subarray(1);
    if (bytes.length > size) throw new Error("integer too long");
    raw.set(bytes, (i + 1) * size - bytes.length);
  });
  return raw;
}
