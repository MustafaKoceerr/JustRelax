/** Base64url without padding, as used in tokens. */
export function base64UrlEncode(bytes: Uint8Array): string {
  let binary: string = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

export function base64UrlDecode(text: string): Uint8Array | null {
  if (!/^[A-Za-z0-9_-]*$/.test(text)) return null;
  const base64: string = text.replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(text.length / 4) * 4, "=");
  try {
    return Uint8Array.from(atob(base64), (char: string): number => char.charCodeAt(0));
  } catch {
    return null;
  }
}

export async function sha256Hex(text: string): Promise<string> {
  const digest: ArrayBuffer = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text));
  return [...new Uint8Array(digest)].map((byte: number): string => byte.toString(16).padStart(2, "0")).join("");
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}
