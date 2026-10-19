import Foundation
import Shared

/// The sound editor's words that depend on state.
enum EditorText {
    static func problem(_ problem: TrimProblem) -> String {
        switch problem {
        case .tooShort: String(localized: .mysoundsProblemTooShort)
        case .tooShortForBed: String(localized: .mysoundsProblemTooShortForBed)
        case .tooLongForEvent: String(localized: .mysoundsProblemTooLongForEvent)
        case .outsideFile: String(localized: .mysoundsProblemOutsideFile)
        }
    }

    /// "start – end · length", each as `m:ss.t` like Android.
    static func selection(_ range: TrimRange) -> String {
        String(localized: .mysoundsSelection(
            ClipTimeKt.formatClipTime(ms: range.startMs),
            ClipTimeKt.formatClipTime(ms: range.endMs),
            ClipTimeKt.formatClipTime(ms: range.lengthMs)
        ))
    }

    /// What the chosen kind does, or why a loop is off for a short file.
    static func kindHint(kind: SoundKind, allowed: Set<SoundKind>) -> String {
        if !allowed.contains(.bed) { return String(localized: .mysoundsKindBedUnavailable) }
        return kind == .bed ? String(localized: .mysoundsKindBedHint) : String(localized: .mysoundsKindEventHint)
    }

    static func message(_ message: SoundEditorMessage) -> String {
        switch message {
        case .saveFailed: String(localized: .mysoundsSaveFailed)
        }
    }
}
