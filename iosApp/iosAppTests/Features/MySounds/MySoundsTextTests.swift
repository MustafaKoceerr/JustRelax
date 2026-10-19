import Foundation
import Shared
import Testing
@testable import JustRelax

struct MySoundsTextTests {
    @Test func aFileThatWontOpenSaysSo() {
        #expect(MySoundsText.message(.importFailed) == String(localized: .mysoundsImportFailed))
    }

    @Test func aSoundShowsItsKindAndLength() {
        let loop = MySoundItem(id: "a", name: "Lake", kind: .bed, icon: .waves, durationMs: 30_000)
        let shot = MySoundItem(id: "b", name: "Robin", kind: .event, icon: .bird, durationMs: 4_000)

        #expect(MySoundsText.details(of: loop) == "\(String(localized: .mysoundsKindBed)) · 0:30")
        #expect(MySoundsText.details(of: shot) == "\(String(localized: .mysoundsKindEvent)) · 0:04")
    }

    @Test func everyTrimProblemUsesItsAndroidText() {
        let problems: [TrimProblem] = [.tooShort, .tooShortForBed, .tooLongForEvent, .outsideFile]

        #expect(problems.map(EditorText.problem) == [
            String(localized: .mysoundsProblemTooShort),
            String(localized: .mysoundsProblemTooShortForBed),
            String(localized: .mysoundsProblemTooLongForEvent),
            String(localized: .mysoundsProblemOutsideFile),
        ])
    }

    @Test func theSelectionShowsStartEndAndLength() {
        #expect(EditorText.selection(TrimRange(startMs: 2_000, endMs: 32_500)) == String(localized: .mysoundsSelection("0:02.0", "0:32.5", "0:30.5")))
    }

    @Test func aLoopTooShortForTheFileSaysWhyItIsOff() {
        #expect(EditorText.kindHint(kind: .bed, allowed: [.event]) == String(localized: .mysoundsKindBedUnavailable))
        #expect(EditorText.kindHint(kind: .bed, allowed: [.bed, .event]) == String(localized: .mysoundsKindBedHint))
        #expect(EditorText.kindHint(kind: .event, allowed: [.bed, .event]) == String(localized: .mysoundsKindEventHint))
    }
}
