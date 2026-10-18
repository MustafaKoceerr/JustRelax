export const PACKAGE_NAME: string = "com.mustafakoceerr.justrelax";

/** A Play Integrity verdict for the genuine app on a genuine device; [overrides] replace whole sections. */
export function verdict(requestHash: string, timestampMillis: number, overrides: Record<string, unknown> = {}): unknown {
  return {
    requestDetails: { requestPackageName: PACKAGE_NAME, requestHash, timestampMillis: String(timestampMillis) },
    appIntegrity: { appRecognitionVerdict: "PLAY_RECOGNIZED", packageName: PACKAGE_NAME },
    deviceIntegrity: { deviceRecognitionVerdict: ["MEETS_DEVICE_INTEGRITY"] },
    ...overrides,
  };
}
