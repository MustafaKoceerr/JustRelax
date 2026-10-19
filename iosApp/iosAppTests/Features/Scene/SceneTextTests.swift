import Foundation
import Shared
import Testing
@testable import JustRelax

struct SceneTextTests {
    @Test func aSavedMixIsNamed() {
        #expect(SceneText.message(SceneMessageSaved(name: "Evening rain")) == String(localized: .msgMixSavedSuccess("Evening rain")))
    }

    @Test func everyProblemHasItsOwnText() {
        let messages: [SceneMessage] = [
            SceneMessageEmptyName.shared,
            SceneMessageNameTaken.shared,
            SceneMessageNothingPlaying.shared,
            SceneMessageSaveFailed.shared,
        ]

        #expect(messages.map(SceneText.message) == [
            String(localized: .errMixSaveEmptyName),
            String(localized: .errMixSaveNameExists),
            String(localized: .errMixSaveNoSound),
            String(localized: .errMixSaveFailed),
        ])
    }
}
