import Foundation
import Shared
import Testing
@testable import JustRelax

struct SavedTextTests {
    @Test func aDeletedMixCanBeTakenBack() {
        #expect(SavedText.message(SavedMessageDeleted(name: "Evening rain")) == String(localized: .msgMixDeleted))
    }

    @Test func aMixThatWontPlaySaysSo() {
        #expect(SavedText.message(SavedMessagePlayFailed.shared) == String(localized: .errPlayMixFailed))
    }

    @Test func theMetadataCountsTheSoundsAndShowsTheDate() {
        let date = Date(timeIntervalSince1970: 1_760_000_000)
        let shownDate = date.formatted(date: .numeric, time: .omitted)

        #expect(SavedText.metadata(soundCount: 3, date: date) == String(localized: .savedMixMetadata(3, shownDate)))
    }

    @Test func aCoverKeepsItsColourForTheSameName() {
        #expect(SavedCover.paletteIndex(for: "Evening rain") == SavedCover.paletteIndex(for: "Evening rain"))
    }

    @Test func coversSpreadOverThePalette() {
        let names = ["Evening rain", "Forest morning", "Deep focus", "Café", "Storm", "Ocean", "Night"]

        #expect(Set(names.map(SavedCover.paletteIndex(for:))).count > 1)
        #expect(names.map(SavedCover.paletteIndex(for:)).allSatisfy { (0..<SavedCover.paletteCount).contains($0) })
    }
}
