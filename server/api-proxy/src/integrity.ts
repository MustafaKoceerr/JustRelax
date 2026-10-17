import { isRecord, sha256Hex } from "./encoding";

export interface IntegrityPolicy {
  packageName: string;
  /** How old an integrity token may be. */
  maxAgeMs: number;
}

export type VerdictResult =
  | { ok: true }
  | { ok: false; reason: "malformed" | "package" | "request_hash" | "stale" | "app" | "device" };

/**
 * The request hash the app puts in its integrity request for a session. It binds the token to
 * this install, so a token cannot be replayed for another one.
 */
export async function sessionRequestHash(installId: string): Promise<string> {
  return sha256Hex(`justrelax-session:${installId}`);
}

/**
 * Checks a decoded Play Integrity verdict (`tokenPayloadExternal`): it is for our app and this
 * request, it is recent, the app is the one Google Play distributes and the device passes integrity.
 */
export function evaluateVerdict(payload: unknown, expectedHash: string, nowMs: number, policy: IntegrityPolicy): VerdictResult {
  if (!isRecord(payload) || !isRecord(payload.requestDetails)) return { ok: false, reason: "malformed" };
  const details: Record<string, unknown> = payload.requestDetails;

  if (details.requestPackageName !== policy.packageName) return { ok: false, reason: "package" };
  if (details.requestHash !== expectedHash) return { ok: false, reason: "request_hash" };

  const timestamp: number = Number(details.timestampMillis);
  if (!Number.isFinite(timestamp) || nowMs - timestamp > policy.maxAgeMs) return { ok: false, reason: "stale" };

  const app: unknown = payload.appIntegrity;
  if (!isRecord(app) || app.appRecognitionVerdict !== "PLAY_RECOGNIZED") return { ok: false, reason: "app" };

  const device: unknown = payload.deviceIntegrity;
  const labels: unknown = isRecord(device) ? device.deviceRecognitionVerdict : undefined;
  if (!Array.isArray(labels) || !labels.includes("MEETS_DEVICE_INTEGRITY")) return { ok: false, reason: "device" };

  return { ok: true };
}
