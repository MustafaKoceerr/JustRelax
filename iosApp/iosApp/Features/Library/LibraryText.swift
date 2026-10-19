import Foundation
import Shared

/// The library's words that depend on state: the greeting and the one-off messages.
enum LibraryText {
    /// `hour` is 0–23 in the device's time zone.
    static func greeting(hour: Int) -> LocalizedStringResource {
        switch TimeOfDayKt.timeOfDayAt(hour: Int32(hour)) {
        case .morning: .greetingMorning
        case .afternoon: .greetingAfternoon
        case .evening: .greetingEvening
        case .night: .greetingNight
        }
    }

    static func message(_ message: LibraryMessage) -> String {
        switch onEnum(of: message) {
        case .downloadFailed: String(localized: .downloadFailed)
        case .text(let text): text.value
        }
    }
}
