import Foundation
import Shared

/// My sounds' message and each sound's kind and length.
enum MySoundsText {
    static func message(_ message: MySoundsMessage) -> String {
        switch message {
        case .importFailed: String(localized: .mysoundsImportFailed)
        }
    }

    /// "Loop · 0:30" or "One-shot · 0:04".
    static func details(of sound: MySoundItem) -> String {
        let kind = sound.kind == .bed ? String(localized: .mysoundsKindBed) : String(localized: .mysoundsKindEvent)
        return "\(kind) · \(ClipTimeKt.formatClipLength(ms: sound.durationMs))"
    }
}
