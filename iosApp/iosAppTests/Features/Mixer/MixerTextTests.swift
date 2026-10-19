import Foundation
import Shared
import Testing
@testable import JustRelax

struct MixerTextTests {
    @Test func aFailedShuffleSaysWhy() {
        #expect(MixerText.message(.generateFailed) == String(localized: .errMixerGenerateFailed))
    }

    @Test func theCountsAreTwoToFive() {
        #expect(MixerText.counts == [2, 3, 4, 5])
    }

    @Test func countsBelowTheLockedSoundsCannotBePicked() {
        #expect(MixerText.counts.filter { MixerText.canPick($0, minimum: 4) } == [4, 5])
    }
}
