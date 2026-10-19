import Foundation
import Shared

/// Why an AI mix could not be made, in the app's language.
enum AiText {
    static func message(_ message: AiMessage) -> String {
        switch message {
        case .noSounds: String(localized: .errAiNoSounds)
        case .emptyResponse: String(localized: .errAiEmptyResponse)
        case .offline: String(localized: .errAiOffline)
        case .busy: String(localized: .errAiBusy)
        case .unverified: String(localized: .errAiUnverified)
        case .unavailable: String(localized: .errAiUnavailable)
        case .unknown: String(localized: .errUnknown)
        }
    }
}
