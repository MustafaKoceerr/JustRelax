import Foundation
import Shared

/// The saved mixes' messages and their metadata line.
enum SavedText {
    static func message(_ message: SavedMessage) -> String {
        switch onEnum(of: message) {
        case .deleted: String(localized: .msgMixDeleted)
        case .playFailed: String(localized: .errPlayMixFailed)
        }
    }

    /// "3 Sounds • 19.10.2026", with the date in the user's format.
    static func metadata(soundCount: Int, date: Date) -> String {
        String(localized: .savedMixMetadata(soundCount, date.formatted(date: .numeric, time: .omitted)))
    }
}
