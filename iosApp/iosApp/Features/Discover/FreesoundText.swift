import Foundation
import Shared

/// Freesound's messages and a result's credit line.
enum FreesoundText {
    static func message(_ message: FreesoundMessage) -> String {
        switch message {
        case .rateLimited: String(localized: .freesoundRateLimited)
        case .offline: String(localized: .freesoundOffline)
        case .unverified: String(localized: .freesoundUnverified)
        case .searchFailed: String(localized: .freesoundFailed)
        case .downloadFailed: String(localized: .freesoundDownloadFailed)
        }
    }

    /// "by author · CC BY 4.0 · 0:42", from the same helpers as Android.
    static func details(of sound: FreesoundSound) -> String {
        let length = ClipTimeKt.formatClipLength(ms: sound.durationMs)
        return "\(String(localized: .freesoundBy(sound.author))) · \(sound.license.label) · \(length)"
    }
}
